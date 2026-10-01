package com.geneticplanner.config

import com.geneticplanner.domain.validation.ProblemValidator
import com.geneticplanner.genetic.GeneticEngine
import com.geneticplanner.genetic.JeneticsEngine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneticEngineConfiguration {

    @Bean
    fun problemValidator(): ProblemValidator =
        ProblemValidator()

    @Bean
    fun geneticEngine(): GeneticEngine =
        JeneticsEngine()
}