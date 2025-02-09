package com.stslex.compiler_app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.stslex.compiler_app.app.R
import com.stslex.compiler_app.model.UserModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels { MainActivityViewModelFactory() }

    private val logger = Logger.getLogger("KotlinCompilerLogger")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.userInfo
            .onEach(::setUI)
            .launchIn(lifecycleScope)

        setClickListeners()
    }

    private fun setClickListeners() {
        findViewById<Button>(R.id.usernameChangeButton).setOnClickListener {
            logger.log(Level.INFO, "usernameChangeButton clicked")
            val randomInt = Random.nextInt()
            viewModel.setName("John $randomInt")
        }
    }

    private fun setUI(user: UserModel) {
        logger.log(Level.INFO, "Setting UI with user: $user")
        sendToastOfUserChanges(user)
        findViewById<TextView>(R.id.usernameFieldTextView).text = user.name
    }

    private fun sendToastOfUserChanges(userModel: UserModel) {
        val textMsg = userModel.getChangesValue(user)
        Toast.makeText(this, textMsg, Toast.LENGTH_SHORT).show()
        user = userModel
    }

    companion object {

        private var user: UserModel? = null
    }
}

