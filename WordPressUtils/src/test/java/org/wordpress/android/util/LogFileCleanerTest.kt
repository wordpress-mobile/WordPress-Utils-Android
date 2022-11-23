package org.wordpress.android.util

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wordpress.android.util.helpers.logfile.LogFileCleaner
import org.wordpress.android.util.helpers.logfile.LogFileProvider
import java.io.File
import java.io.FileReader
import kotlin.random.Random

/**
 *  The number of test files to create for each test run
 */
private const val MAX_FILES = 10

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class LogFileCleanerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val testProvider = LogFileProvider.fromContext(context)

    @Before
    fun setup() {
        repeat(MAX_FILES) {
            val file = File(testProvider.getLogFileDirectory(), "$it.log")
            file.writeText("$it")
            file.setLastModified(it * 10_000L)
        }

        assert(testProvider.getLogFileDirectory().listFiles().count() == MAX_FILES)
    }

    @After
    fun tearDown() {
        // Delete the test directory after each test
        testProvider.getLogFileDirectory().deleteRecursively()
    }

    @Test
    fun testThatCleanerPreservesMostRecentlyCreatedFiles() {
        val maxLogFileCount = Random.nextInt(MAX_FILES)
        LogFileCleaner(testProvider, maxLogFileCount).clean()

        // Strings are easier to assert against than arrays
        val remainingFileIds = testProvider.getLogFiles().joinToString(",") {
            FileReader(it).readText()
        }

        val expectedValue = (MAX_FILES - 1 downTo 0).take(maxLogFileCount).reversed().joinToString(",")
        assertEquals(expectedValue, remainingFileIds)
    }

    @Test
    fun testThatCleanerPreservesCorrectNumberOfFiles() {
        val numberOfFiles = Random.nextInt(MAX_FILES)
        LogFileCleaner(testProvider, numberOfFiles).clean()
        assertEquals(numberOfFiles, testProvider.getLogFileDirectory().listFiles().count())
    }

    @Test
    fun testThatCleanerErasesAllFilesIfGivenZero() {
        LogFileCleaner(testProvider, 0).clean()
        assert(testProvider.getLogFileDirectory().listFiles().isEmpty())
    }
}
