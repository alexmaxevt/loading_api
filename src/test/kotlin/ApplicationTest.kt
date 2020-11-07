import com.jayway.jsonpath.JsonPath
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.*
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import ru.evtukhov.ru.evtukhov.module

class ApplicationTest {
    private val jsonContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
    private val configure: Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("ncraft.jwt.secret", "secret")
            put("ncraft.upload.dir", createTempFile().absolutePath)
        }
        module()
    }

    @Test
    fun testUnauthorized() {
        withTestApplication(configure) {
            with(handleRequest(HttpMethod.Get, "/api/v1/posts")) {
                response
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    @Test
    fun testAuth() {
        withTestApplication(configure) {
            runBlocking {
                var token: String? = null
                with(handleRequest(HttpMethod.Post, "/api/v1/authentication") {
                    addHeader(HttpHeaders.ContentType, jsonContentType.toString())
                    setBody(
                        """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
                    )
                }) {
                    println(response.content)
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = JsonPath.read<String>(response.content!!, "$.token")
                }
                delay(500)
                with(handleRequest(HttpMethod.Get, "/api/v1/posts") {
                    addHeader(HttpHeaders.Authorization, "Bearer $token")
                }) {
                    response
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }
}