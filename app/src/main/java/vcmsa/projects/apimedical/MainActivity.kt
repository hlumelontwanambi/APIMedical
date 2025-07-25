package vcmsa.projects.apimedical

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var outputTextView: TextView
    private lateinit var  inputEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        outputTextView = findViewById(R.id.txtOutput)
        inputEditText = findViewById(R.id.txtEnterLoan)
    }

    private fun getAllLoans(){
        //Define the API endpoint URl.
        val url = "https://opsc.azurewebsites.net/loans/"
        outputTextView.text = "Fetching all loans..."

        //Execute the network request on a background thread.
        executor.execute{
            //Use Fuel's hhtpGet for a GET request.
            url.httpGet().responseString{ _, _, result ->
                // Switch to the main thread to update the UI.
                handler.post {
                    when(result) {
                        is com.github.kittinunf.result.Result.Success -> {
                            //On success, deserialize the JSON string into a list of Loan objects.
                            val json = result.get()
                            try {
                                val loans = Gson().fromJson(json, Array<BookLoan.Loan>::class.java).toList()
                                if (loans.isNotEmpty()){
                                    //Format the output for readability.
                                    val formattedOutput = loans.joinToString(separator = "\n\n") { loan -> "Loan ID: ${loan.loanID}\nAmount: " +
                                            "${loan.amount}\nMember ID: " + "${loan.memberID}\nMessage: ${loan.message}"
                                    }
                                    outputTextView.text = formattedOutput
                                } else {
                                    outputTextView.text = "No loans found."
                                }
                            }catch (e: JsonSyntaxException){
                                // Handle cases where the server response is not valid JSON.
                                Log.e("GetAllloans", "JSON parsing error: ${e.message}")
                                outputTextView.text = "Error: Could not parse server response."
                            }
                        }
                        is Result.Failure -> {
                            //On failure, log the error and show a user-friendly message.
                            val ex = result.getException()
                            Log.e("GetAllLoans", "API Error: ${ex.message}")
                            outputTextView.text = "Error: Could not fetch loans from the server."
                        }
                    }
                }
            }
        }
    }
}