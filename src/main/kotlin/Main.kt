package io.joker
import com.beust.klaxon.*
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet

fun main(args: Array<String>) {

    println("You'll get the API usage rules of the best quality.");

    getPublicJavaRepositoriesList();

}

fun getPublicJavaRepositoriesList() {

    var pageNumber=1;

    FuelManager.instance.basePath = "https://api.github.com";

    var fullRepositoriesList: MutableList<String> = emptyList<String>().toMutableList()

    do {

        val (request, response, result) = "/search/repositories".httpGet(listOf(Pair("q","language:java"),Pair("sort","stars"),
                Pair("page",pageNumber))).responseString()

        println(request)

        if (!response.httpStatusCode.equals(200))
            throw Exception("List of public git repositories is not available. Code: ${response.httpStatusCode}");

        var repositoriesList = parseRepositories(result.component1());

        fullRepositoriesList.addAll(repositoriesList)

        pageNumber=pageNumber+1

        if (pageNumber %10==0) Thread.sleep(60000);

    }while (repositoriesList.size!=0)

    fullRepositoriesList.map{value ->println("Repository: ${value}")};

}

fun  parseRepositories(response: String?): MutableList<String> {

    var result= emptyList<String>().toMutableList();

    val parser: Parser = Parser()

    if (!response.isNullOrBlank()) {
        val responseSb: StringBuilder = StringBuilder(response)
        val json: JsonObject = parser.parse(responseSb) as JsonObject
        json.lookup<String?>("items.full_name").map { value -> value?.let { result.add(it) } }
    }
    return result
}
