package io.github.mklkj.agp_mock_compatibility

import io.mockk.every
import io.mockk.mockk
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

    @Test
    fun addtionTest_mock() {
        val calculatorMock = mockk<Calculator>()
        every { calculatorMock.addition(any(), any()) } returns 5
        assertEquals(5, calculatorMock.addition(2, 2))
    }
}
