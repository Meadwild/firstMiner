package io.joker
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.int
import com.beust.klaxon.lookup
import com.github.debop.kodatimes.now
import com.github.debop.kodatimes.toIsoFormatDateString
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.failure
import org.joda.time.DateTime


val username = "Meadwild"
val password = "4069043r"
var currentNumberOfQueries=0
var totalNumberOfQueries=0
var fullRepositoriesList: MutableList<String> = emptyList<String>().toMutableList()

fun main(args: Array<String>) {

    println("You'll get the API usage rules of the best quality.");

    FuelManager.instance.basePath = "https://api.github.com";

    getPublicJavaRepositoriesList();

}

fun askGitForPortionOfRepositories(timeStartFilter : DateTime, timeEndFilter : DateTime, page : Int): Result<String, FuelError> {

    val (request, response, result) = "/search/repositories".httpGet(listOf(Pair("q","language:java+created:\"${timeStartFilter.toIsoFormatDateString()}+..+${timeEndFilter.toIsoFormatDateString() }\""),Pair("sort","stars"),
            Pair("page","${page}"), Pair("per_page",100))).
            authenticate(username, password).response {
        _, _, result ->
        result.failure {throw Exception("Application isn't authenticated.")
        }
    }.responseString()

    println(request)

    if (!response.httpStatusCode.equals(200))
        throw Exception("List of public git repositories is not available. Code: ${response.httpStatusCode}. " +
                "Message: ${response.httpResponseMessage}");

    return result
}

fun logQuery(){

    println("Number of query: ${totalNumberOfQueries+ currentNumberOfQueries}")
    println("Current number of repos: ${ fullRepositoriesList.size}")
}

fun controlQuery(){
    currentNumberOfQueries++

    if (currentNumberOfQueries >=10){

        totalNumberOfQueries= totalNumberOfQueries+ currentNumberOfQueries;
        currentNumberOfQueries=0;
        println("Sleep to respect the rules...")
        Thread.sleep(60000);
    }

}


fun getPublicJavaRepositoriesList() {


    var currentTimeStart= DateTime(0)
    var currentTimeEnd= DateTime.now()
    var futureTimeStart= currentTimeStart
    var futureTimeEnd=currentTimeEnd


    var stopFlag=false;

    do {

        logQuery()

        currentTimeEnd=futureTimeEnd
        currentTimeStart=futureTimeStart

        var result=askGitForPortionOfRepositories(currentTimeStart, currentTimeEnd,1);

        controlQuery()

        var parsedRepositoriesList = parseRepositories(result.component1());


        if (parsedRepositoriesList.first>1000)
        {
            println("There are too much ${parsedRepositoriesList.first} repositories are found in time range: ${currentTimeStart} - ${currentTimeEnd}")
            futureTimeEnd=DateTime(currentTimeEnd.millis -((currentTimeEnd.millis - currentTimeStart.millis)/2))
            continue;
        }else{

            if (currentTimeEnd.equals(DateTime.now()))
                break;


            futureTimeStart=currentTimeEnd;
            futureTimeEnd= DateTime(currentTimeEnd.millis+(currentTimeEnd.millis-currentTimeStart.millis))

            println("1000 or less repositories are found in time range: ${currentTimeStart} - ${currentTimeEnd}")
            println("We will ask them (${parsedRepositoriesList.first}) more precisely (with pagination)")

            fullRepositoriesList.addAll(parsedRepositoriesList.second)

            var page=2;

            do{
                println("Page number ${page} is processed")

                logQuery()

                result=askGitForPortionOfRepositories(currentTimeStart,currentTimeEnd,page);

                controlQuery()

                parsedRepositoriesList = parseRepositories(result.component1());

                fullRepositoriesList.addAll(parsedRepositoriesList.second)

                page++;
            }
            while ((parsedRepositoriesList.second.size>0)&&(page!=11))
        }

    }while (true)

    fullRepositoriesList.map{value ->println("Repository: ${value}")};

}

fun  parseRepositories(response: String?): Pair<kotlin.Int, MutableList<String>> {

    var resultList= emptyList<String>().toMutableList();
    var quantityOfResults=0;

    val parser: Parser = Parser()

    if (!response.isNullOrBlank()) {
        val responseSb: StringBuilder = StringBuilder(response)
        val json: JsonObject = parser.parse(responseSb) as JsonObject
        quantityOfResults= json.int("total_count")!!
        json.lookup<String?>("items.full_name").map { value -> value?.let { resultList.add(value) } }
    }
    return Pair(quantityOfResults,resultList)
}
