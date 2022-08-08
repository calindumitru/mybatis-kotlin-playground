package it.dumitru.mybatiskotlinplayground

import mu.KotlinLogging
import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
class CarConfiguration {
    private val log = KotlinLogging.logger {}


    @Profile("cars")
    @Bean("Cars")
    fun init(carMapper: CarMapper) = CommandLineRunner {
        log.info { "=== Deleting all cars ===" }
        carMapper.deleteAll()

        log.info { "=== Inserting cars ===" }
        carMapper.insertCar(Car(make = "BMW", model = "I don't know any BMW models", constructionYear = 2022))
        carMapper.insertCar(Car(make = "Volkswagen", model = "Golf", constructionYear = 1993))
        carMapper.insertCar(Car(make = "Fiat", model = "Panda", constructionYear = 2015))
        carMapper.insertCar(Car(make = "Volkswagen", model = "Polo", constructionYear = 2003))

        printAllCars(carMapper)

        log.info { "=== Find all Volkswagen cars ===" }
        carMapper.findByMake("Volkswagen").forEach { log.info { "Car: $it" } }
        log.info { "=== Searching... ===" }
        carMapper.searchByModelContaining("o").forEach { log.info { "Car: $it" } }
    }

    private fun printAllCars(carMapper: CarMapper) {
        log.info { "=== Printing cars ===" }
        carMapper.selectAll().forEach { log.info { "Car: $it" } }
    }
}

data class Car(var id: Int = 0, val make: String, val model: String, val constructionYear: Int)

@Mapper
interface CarMapper {
    @Select("SELECT * FROM CAR")
    fun selectAll(): List<Car>

    @Delete("DELETE FROM CAR")
    fun deleteAll()

    @Insert("INSERT INTO CAR(make, model, construction_year) values (#{make}, #{model}, #{constructionYear})")
    fun insertCar(car: Car)

    @Select("SELECT * FROM CAR WHERE make = #{make}")
    fun findByMake(make: String): List<Car>

    @Select("SELECT * FROM CAR WHERE model LIKE '%' || #{term} || '%'")
    fun searchByModelContaining(term: String): List<Car>
}
