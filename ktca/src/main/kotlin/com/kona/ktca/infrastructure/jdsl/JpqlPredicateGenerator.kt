package com.kona.ktca.infrastructure.jdsl

import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates
import java.time.LocalDateTime
import kotlin.reflect.KProperty1

object JpqlPredicateGenerator {

    fun <T : Any, E: Any> whereEqualTo(field: T, column: KProperty1<E, T?>): Predicatable {
        return Predicates.equal(value = Paths.path(column), compareValue = Expressions.value(field))
    }

    fun <T : Any, E: Any> whereEqualTo(property: KProperty1<E, T?>, value: T?): Predicatable? {
        return value?.let { Predicates.equal(value = Paths.path(property), compareValue = Expressions.value(it)) }
    }

    fun <E: Any> greaterThanOrEqualTo(property: KProperty1<E, LocalDateTime?>, value: LocalDateTime): Predicatable {
        return Predicates.greaterThanOrEqualTo(value = Paths.path(property), compareValue = Expressions.value(value))
    }

    fun <E: Any> lessThanOrEqualTo(property: KProperty1<E, LocalDateTime?>, value: LocalDateTime): Predicatable {
        return Predicates.lessThanOrEqualTo(value = Paths.path(property), compareValue = Expressions.value(value))
    }

}