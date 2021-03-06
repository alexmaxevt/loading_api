package ru.evtukhov.service

import io.ktor.features.NotFoundException
import io.ktor.util.KtorExperimentalAPI
import ru.evtukhov.model.UserModel
import org.springframework.security.crypto.password.PasswordEncoder
import ru.evtukhov.repository.UserRepository
import ru.evtukhov.dto.AuthenticationRequestDto
import ru.evtukhov.dto.AuthenticationResponseDto
import ru.evtukhov.dto.PasswordChangeRequestDto
import ru.evtukhov.dto.UserResponseDto
import ru.evtukhov.exception.InvalidPasswordException
import ru.evtukhov.exception.NullUsernameOrPasswordException
import ru.evtukhov.exception.PasswordChangeException
import ru.evtukhov.exception.UserExistsException
import ru.evtukhov.service.JWTTokenService

class UserService(
    private val repo: UserRepository,
    private val tokenService: JWTTokenService,
    private val passwordEncoder: PasswordEncoder
) {

    suspend fun getByUserName(username: String): UserModel? {
        return repo.getByUsername(username)
    }

    suspend fun getModelByIdPassword(id: Long, password: String): UserModel? {
        return repo.getByIdPassword(id, password)
    }

    suspend fun getModelById(id: Long): UserModel? {
        return repo.getById(id)
    }

    @KtorExperimentalAPI
    suspend fun getById(id: Long): UserResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        return UserResponseDto.fromModel(model)
    }

    @KtorExperimentalAPI
    suspend fun changePassword(id: Long, input: PasswordChangeRequestDto): AuthenticationResponseDto {
        val model = repo.getById(id) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.old, model.password)) {
            throw PasswordChangeException("Wrong password!")
        }
        val copy = model.copy(password = passwordEncoder.encode(input.new))
        repo.save(copy)
        val token = tokenService.generate(copy)
        return AuthenticationResponseDto(token)
    }

    @KtorExperimentalAPI
    suspend fun authenticate(input: AuthenticationRequestDto): AuthenticationResponseDto {
        val model = repo.getByUsername(input.username) ?: throw NotFoundException()
        if (!passwordEncoder.matches(input.password, model.password)) {
            throw InvalidPasswordException("Wrong password!")
        }
        val token = tokenService.generate(model.copy())
        return AuthenticationResponseDto(token)
    }

    suspend fun save(username: String, password: String): AuthenticationResponseDto {
        if (username == "" || password == "") {
            throw NullUsernameOrPasswordException("Username or password is empty")
        } else if (repo.getByUsername(username) != null) {
            throw UserExistsException("User already exists")
        } else {
            val model = repo.save(UserModel(username = username, password = passwordEncoder.encode(password)))
            val token = tokenService.generate(model)
            return AuthenticationResponseDto(token)
        }
    }
}