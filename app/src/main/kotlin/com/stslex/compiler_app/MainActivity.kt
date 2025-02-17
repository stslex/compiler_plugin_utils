package com.stslex.compiler_app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.stslex.compiler_app.UserToastUtil.sendToastOfUserChanges
import com.stslex.compiler_app.app.R
import com.stslex.compiler_app.model.UserModel
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun
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
            viewModel.setName("Name $randomInt")
        }
        findViewById<Button>(R.id.secondNameChangeButton).setOnClickListener {
            logger.log(Level.INFO, "secondNameChangeButton clicked")
            val randomInt = Random.nextInt()
            viewModel.setSecondName("SecondName $randomInt")
        }
    }

    private fun setUI(user: UserModel) {
        logger.log(Level.INFO, "Setting UI with user: $user")
        sendToastOfUserChanges(user)
        setName(user.name)
        setSecondName(user.secondName)
        printUsernameWithSingletonDistinct(user.name)
    }


    @DistinctUntilChangeFun(true)
    private fun setName(name: String) {
        logger.log(Level.INFO, "setName: $name")
        findViewById<TextView>(R.id.usernameFieldTextView).text = name
    }

    @DistinctUntilChangeFun(true, action = TestLogger::class)
    private fun setSecondName(name: String) {
        logger.log(Level.INFO, "setSecondName: $name")
        findViewById<TextView>(R.id.secondNameFieldTextView).text = name
    }
}

@DistinctUntilChangeFun(
    logging = true,
    singletonAllow = true
)
private fun printUsernameWithSingletonDistinct(name: String) {
    println("printUsernameWithSingletonDistinct: $name")
}


