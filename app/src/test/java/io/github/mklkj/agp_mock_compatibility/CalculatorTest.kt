package io.github.mklkj.agp_mock_compatibility

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculatorTest {

    private lateinit var calculator: Calculator

    @Before
    fun setUp() {
        calculator = Calculator()
    }

    @Test
    fun additionTest() {
        assertEquals(4, calculator.addition(2, 2))
    }
}
