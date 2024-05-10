package com.example.security.factorial;

public class JavaFactorial {
    public static String calculate(int n) {
        int factorial = 1;
        for (int i = 1; i <= n; ++i) {
            factorial *= i;
        }

        return String.valueOf(factorial);
    }
}
