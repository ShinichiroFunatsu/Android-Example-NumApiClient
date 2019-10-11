package app.sfunatsu.numberapiclient.repository

import androidx.lifecycle.liveData
import app.sfunatsu.numberapiclient.model.NumTrivia
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class NumTriviaRepository(
    private val remoteDataSource: NumTriviaRemoteDataSource = NumTriviaRemoteDataSource()
) {
    suspend fun findNumOfTrivia(num: Long): GetNumTriviaResult<Exception> = GetNumTriviaResult.runCatching {
        return@runCatching remoteDataSource.triviaOf(num)
    }

    fun findNumOfTriviaLiveData(num: Long) = liveData {
        this.emit(findNumOfTrivia(num))
    }
}

sealed class GetNumTriviaResult<out T> {
    data class Success(val trivia: NumTrivia) : GetNumTriviaResult<Nothing>()
    data class Error<out T>(val e: T) : GetNumTriviaResult<T>()
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

    suspend fun triviaOf(num: Long): NumTrivia = service.triviaOf(num = num)

}

const val BASE_URL = "http://numbersapi.com/"

interface NumApiService {
    @GET("{num}?json")
    suspend fun triviaOf(@Path("num") num: Long): NumTrivia
}