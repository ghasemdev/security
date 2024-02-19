package com.parsuomash.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class RSABenchmark {

  @get:Rule
  val benchmarkRule = BenchmarkRule()

  @Test
  fun generateKeyPair() {
    benchmarkRule.measureRepeated {
      val name = runWithTimingDisabled { Random.nextInt().toString() }
      KeyStoreHelper.generateKeyPair(name)
    }
  }

  @Test
  fun getKeyPair() {
    benchmarkRule.measureRepeated {
      val name = runWithTimingDisabled { Random.nextInt().toString() }
      runWithTimingDisabled { KeyStoreHelper.generateKeyPair(name) }
      KeyStoreHelper.getKeyPair(name)
    }
  }

  @Test
  fun sign() {
    benchmarkRule.measureRepeated {
      val name = runWithTimingDisabled { Random.nextInt().toString() }
      val keyPair = runWithTimingDisabled { KeyStoreHelper.generateKeyPair(name) }
      KeyStoreHelper.sign(loremData, keyPair.private)
    }
  }
}
