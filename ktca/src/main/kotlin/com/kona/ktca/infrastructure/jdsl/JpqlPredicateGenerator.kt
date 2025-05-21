package com.kona.ktca.infrastructure.jdsl

import com.linecorp.kotlinjdsl.querymodel.jpql.expression.Expressions
import com.linecorp.kotlinjdsl.querymodel.jpql.path.Paths
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicatable
import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.Predicates
import kotlin.reflect.KProperty1

object JpqlPredicateGenerator {

    fun <T : Any, E: Any> whereEqualTo(field: T, column: KProperty1<E, T>): Predicatable {
        return Predicates.equal(Paths.path(column), Expressions.value(field))
    }

}