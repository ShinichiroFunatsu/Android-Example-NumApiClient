package app.sfunatsu.numberapiclient.repository

import app.sfunatsu.numberapiclient.model.NumTrivia
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class NumTriviaRepository(
    private val remoteDataSource: NumTriviaRemoteDataSource = NumTriviaRemoteDataSource()
) {
    suspend fun fetchNumOfTrivia(num: Long) = GetNumTriviaResult.runCatching {
        remoteDataSource.fetchTriviaOf(num)
    }
}

sealed class GetNumTriviaResult<out T> {
    object Empty: GetNumTriviaResult<Nothing>()
    data class Success(val trivia: NumTrivia) : GetNumTriviaResult<Nothing>()
    data class Error<out T>(val e: T) : GetNumTriviaResult<T>()

    fun onSuccess(f: (NumTrivia) -> Unit) = apply { if (this is Success) f(trivia) }

    companion object {
        suspend fun runCatching(f: suspend () -> NumTrivia) = try {
            Success(f())
        } catch (e: Exception) {
            Error(e)
        }
    }
}

class NumTriviaRemoteDataSource {
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val service: NumApiService = retrofit.create(NumApiService::class.java)

    suspend fun fetchTriviaOf(num: Long): NumTrivia = service.triviaOf(num = num)

}

const val BASE_URL = "http://numbersapi.com/"

interface NumApiService {
    @GET("{num}?json")
    suspend fun triviaOf(@Path("num") num: Long): NumTrivia
}