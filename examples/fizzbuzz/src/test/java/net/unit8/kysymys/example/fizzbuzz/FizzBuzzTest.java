package net.unit8.kysymys.example.fizzbuzz;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 採点テスト。全部通れば scorer は 100 を出す。
 */
class FizzBuzzTest {
    private final FizzBuzz fizzBuzz = new FizzBuzz();

    @Test
    void plainNumbers() {
        assertEquals("1", fizzBuzz.convert(1));
        assertEquals("2", fizzBuzz.convert(2));
        assertEquals("4", fizzBuzz.convert(4));
    }

    @Test
    void multiplesOfThree() {
        assertEquals("Fizz", fizzBuzz.convert(3));
        assertEquals("Fizz", fizzBuzz.convert(6));
        assertEquals("Fizz", fizzBuzz.convert(9));
    }

    @Test
    void multiplesOfFive() {
        assertEquals("Buzz", fizzBuzz.convert(5));
        assertEquals("Buzz", fizzBuzz.convert(10));
        assertEquals("Buzz", fizzBuzz.convert(20));
    }

    @Test
    void multiplesOfFifteen() {
        assertEquals("FizzBuzz", fizzBuzz.convert(15));
        assertEquals("FizzBuzz", fizzBuzz.convert(30));
    }
}
