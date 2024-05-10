package com.example.security.factorial

class KotlinFactorial {
  companion object {
    fun calculate(n: Int): String {
      var factorial = 1
      for (i in 1..n) {
        factorial *= i
      }

      return factorial.toString()
    }
  }
}
