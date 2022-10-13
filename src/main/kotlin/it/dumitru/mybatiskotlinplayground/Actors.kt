package it.dumitru.mybatiskotlinplayground

import it.dumitru.mybatiskotlinplayground.ActorDynamicSqlSupport.actor
import it.dumitru.mybatiskotlinplayground.ActorDynamicSqlSupport.birthDate
import it.dumitru.mybatiskotlinplayground.ActorDynamicSqlSupport.gender
import it.dumitru.mybatiskotlinplayground.ActorDynamicSqlSupport.id
import it.dumitru.mybatiskotlinplayground.ActorDynamicSqlSupport.name
import mu.KotlinLogging
import org.apache.ibatis.annotations.*
import org.mybatis.dynamic.sql.SqlTable
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.sql.JDBCType
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Configuration
class ActorConfiguration {

    @Profile("actors")
    @Bean("Actors")
    fun initActors(actorMapper: ActorMapper) = CommandLineRunner {
        log.info { "=== Inserting actors ===" }
        actorMapper.insertActor(Actor(name = "Brad Pitt", gender = "M", birthDate = LocalDate.of(1963, 12, 18)))
        actorMapper.insertActor(Actor(name = "Chris Pine", gender = "M", birthDate = LocalDate.of(1980, 8, 26)))
        actorMapper.insertActor(Actor(name = "Chris Pratt", gender = "M", birthDate = LocalDate.of(1979, 6, 21)))
        actorMapper.insertActor(Actor(name = "Christian Bale", gender = "M", birthDate = LocalDate.of(1974, 1, 30)))
        actorMapper.insertActor(Actor(name = "Christina Ricci", gender = "F", birthDate = LocalDate.of(1980, 2, 12)))

        actorMapper.selectMany { allRows() }.forEach { log.info { "Actor: $it" } }

        log.info { "=== Selecting specific actors ===" }
        actorMapper.selectMany {
            where {
                name isLike "Chris%"
                and {
                    gender isEqualTo "M"
                }
                and {
                    birthDate isGreaterThan LocalDate.of(1975, 1, 1)
                }
            }
        }.forEach { log.info { "Found actor: $it" } }

        log.info { "=== Select first ===" }
        actorMapper.selectOne {
            limit(1)
        }.let { log.info { "First actor: $it" } }


        log.info { "=== Verbose ===" }
        actorMapper.count(verboseCount()).let { log.info { "Count: $it" } }

        actorMapper.select(verboseSelect()).forEach{ log.info { "VActor: $it" }}
    }

    private fun verboseCount() = count(id) {
        from (actor)
        where { id isBetween 3 and 1000 }
    }

    private fun verboseSelect() = select(columnList) {
        from (actor)
        allRows()
        orderBy(birthDate)
    }
}

data class Actor(
    var id: Int? = 0,
    val name: String,
    val gender: String,
    val birthDate: LocalDate
)

object ActorDynamicSqlSupport {
    val actor = Actor()
    val id = actor.id
    val name = actor.name
    val gender = actor.gender
    val birthDate = actor.birthDate

    class Actor : SqlTable("actor") {
        val id = column<Int>(name = "id", jdbcType = JDBCType.INTEGER)
        val name = column<String>(name = "name", jdbcType = JDBCType.VARCHAR)
        val gender = column<String>(name = "gender", jdbcType = JDBCType.VARCHAR)
        val birthDate = column<LocalDate>(name = "birth_date", jdbcType = JDBCType.DATE)
    }
}


@Mapper
interface ActorMapper : CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Actor>, CommonUpdateMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(
        id = "ActorResult", value = [
            Result(column = "id", property = "id"),
            Result(column = "name", property = "name"),
            Result(column = "gender", property = "gender"),
            Result(column = "birth_date", property = "birthDate"),
        ]
    )
    fun select(selectStatement: SelectStatementProvider): List<Actor>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("ActorResult")
    fun selectOne(selectStatement: SelectStatementProvider): Actor?
}

fun ActorMapper.insertActor(actorRecord: Actor) =
    insert(this::insert, actorRecord, actor) {
        //map(id) toProperty "id"
        map(name) toProperty "name"
        map(gender) toProperty "gender"
        map(birthDate) toProperty "birthDate"
    }

fun ActorMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, actor, completer)

fun ActorMapper.deleteByPrimaryKey(id_: Int) =
    delete {
        where { id isEqualTo id_ }
    }

fun ActorMapper.deleteByName(name_: String) =
    delete {
        where { name isEqualTo name_ }
    }

fun ActorMapper.selectMany(completer: SelectCompleter) =
    selectList(this::select, columnList, actor, completer)

fun ActorMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, actor, completer)

private val columnList = listOf(
    id,
    name,
    gender,
    birthDate
)
