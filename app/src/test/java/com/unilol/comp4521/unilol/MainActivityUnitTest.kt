/*
# COMP 4521 # IYER ARVIND   20218689    aiyer@connect.ust.hk
# COMP 4521 # RYAN BUDI     20214841    bryanaa@connect.ust.hk
# COMP 4521 # JI TONY WAI SUM 20217881  twsji@connect.ust.hk
*/
package com.unilol.comp4521.unilol

import com.unilol.comp4521.unilol.interfaces.Post
import com.unilol.comp4521.unilol.interfaces.searchPost
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Random
import kotlin.collections.ArrayList
import kotlin.collections.addAll
import kotlin.collections.filter

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MainActivityUnitTest {
    private val testPosts = ArrayList<Post>()

    @Before
    fun init() {
        val ids = arrayOf("abcd", "212313213", "0", ":)")
        val titles = arrayOf("test post 1", "test post 2", "Test post 3", "another one")
        val descriptions = arrayOf(
                "first one has some fun images",
                "this meme is cool",
                "like if you agree",
                "just another one for comedy"
        )
        val random = Random()

        for (i: Int in 0..ids.size-1) {
            testPosts.add(Post(
                    id= ids[i],
                    title = titles[i],
                    upvotes = random.nextInt(100) - 20,
                    url = "https://http.cat/${random.nextInt(12) + 409 }",
                    user_id = "user${i}",
                    description= descriptions[i]
            ))
        }

        testPosts[0].tags.addAll(arrayOf("test", "lol"))
        testPosts[1].tags.addAll(arrayOf("testing"))
        testPosts[2].tags.addAll(arrayOf("funny", "cool"))

    }

    @Test
    fun postSearch_titleMatch() {
        assertEquals( 3, testPosts.filter { p -> searchPost(p, "post")}.size)

    }

    @Test
    fun postSearch_titleMatchIgnoreCase() {
        assertEquals(3, testPosts.filter{ p -> searchPost(p, "test")}.size)
    }

    @Test
    fun postSearch_tagAndTitleSearch() {
        val funcount = testPosts.filter { p -> searchPost(p, "fun") }.size
        // Can be one if search only matches full tag
        println("Number of posts matched: ${funcount}")
        assertEquals(true, funcount >= 1)
    }

}
