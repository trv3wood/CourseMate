package com.example.coursemate.network

import com.example.coursemate.network.api.AuthApi
import com.example.coursemate.network.api.HomeworkApi
import com.example.coursemate.network.api.PostApi
import com.example.coursemate.network.dto.HomeworkCreateDto
import com.example.coursemate.network.dto.LoginRequestDto
import com.example.coursemate.network.dto.RegisterRequestDto
import com.example.coursemate.network.dto.ReplyCreateDto
import com.example.coursemate.network.retrofit.AuthInterceptor
import com.example.coursemate.network.retrofit.TokenReader
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitApiTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginParsesTokenAndSendsRequestBody() = runTest {
        server.enqueue(jsonResponse("json/login_success.json"))
        val api = createApi<AuthApi>()

        val token = api.login(LoginRequestDto(username = "alice", password = "secret123"))

        assertEquals("demo-token", token.accessToken)
        assertEquals("bearer", token.tokenType)
        val request = server.takeRequest()
        assertEquals("/auth/login", request.path)
        assertTrue(request.body.readUtf8().contains("\"username\":\"alice\""))
    }

    @Test
    fun loginFailureThrowsHttpException() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"detail":"bad credentials"}"""))
        val api = createApi<AuthApi>()

        try {
            api.login(LoginRequestDto(username = "alice", password = "wrong"))
            fail("Expected login to throw HttpException")
        } catch (exception: HttpException) {
            assertEquals(401, exception.code())
        }
    }

    @Test
    fun registerParsesUserAndSendsRequestBody() = runTest {
        server.enqueue(jsonResponse("json/register_success.json"))
        val api = createApi<AuthApi>()

        val user = api.register(
            RegisterRequestDto(
                username = "alice",
                email = "alice@example.com",
                password = "secret123"
            )
        )

        assertEquals(1, user.id)
        assertEquals("alice@example.com", user.email)
        val requestBody = server.takeRequest().body.readUtf8()
        assertTrue(requestBody.contains("\"email\":\"alice@example.com\""))
        assertTrue(requestBody.contains("\"role\":\"student\""))
    }

    @Test
    fun jwtInterceptorAddsAuthorizationHeader() = runTest {
        server.enqueue(jsonResponse("json/homework_list_success.json"))
        val api = createApi<HomeworkApi>(token = "jwt-token")

        api.listHomework()

        assertEquals("Bearer jwt-token", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun homeworkApiParsesJsonAndSendsCreateBody() = runTest {
        server.enqueue(jsonResponse("json/homework_list_success.json"))
        server.enqueue(jsonResponse("json/homework_create_success.json"))
        val api = createApi<HomeworkApi>()

        val homework = api.listHomework()
        val created = api.createHomework(
            HomeworkCreateDto(
                courseId = 10,
                title = "Lab 1",
                content = "Finish exercises",
                deadline = "2026-05-20T23:59:00Z"
            )
        )

        assertEquals("Essay", homework.first().title)
        assertEquals(99, created.id)
        server.takeRequest()
        val createRequest = server.takeRequest()
        assertEquals("/homework", createRequest.path)
        assertTrue(createRequest.body.readUtf8().contains("\"courseId\":10"))
    }

    @Test
    fun discussionApiParsesPostsRepliesAndAcceptResponse() = runTest {
        server.enqueue(jsonResponse("json/posts_list_success.json"))
        server.enqueue(jsonResponse("json/post_detail_success.json"))
        server.enqueue(jsonResponse("json/reply_success.json"))
        server.enqueue(jsonResponse("json/reply_accepted_success.json"))
        val api = createApi<PostApi>()

        val posts = api.listPosts()
        val detail = api.getPost(7)
        val reply = api.replyToPost(7, ReplyCreateDto(content = "Try a smaller example."))
        val accepted = api.acceptReply(30)

        assertEquals("Need help with recursion", posts.first().title)
        assertEquals(1, detail.replies.size)
        assertEquals("Try a smaller example.", reply.content)
        assertTrue(accepted.isAccepted)
        assertEquals("/posts/7/reply", server.takeRequestAt(2).path)
    }

    private inline fun <reified T> createApi(token: String? = null): T {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(
                    object : TokenReader {
                        override suspend fun currentToken(): String? = token
                    }
                )
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(T::class.java)
    }

    private fun jsonResponse(resourcePath: String): MockResponse {
        return MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(resourceText(resourcePath))
    }

    private fun resourceText(resourcePath: String): String {
        val resource = checkNotNull(javaClass.classLoader?.getResource(resourcePath)) {
            "Missing test resource: $resourcePath"
        }
        return resource.readText()
    }

    private fun MockWebServer.takeRequestAt(index: Int) = repeat(index) { takeRequest() }.let { takeRequest() }
}
