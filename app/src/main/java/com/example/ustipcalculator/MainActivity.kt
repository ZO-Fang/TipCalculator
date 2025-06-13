package com.example.ustipcalculator

import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import android.icu.text.NumberFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ustipcalculator.ui.theme.USTipCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            USTipCalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TipTimeLayout(
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview(modifier:Modifier = Modifier) {
    USTipCalculatorTheme {
        TipTimeLayout(modifier)
    }
}

@Composable
fun TipTimeLayout(modifier:Modifier = Modifier) {
    var amountInput by remember { mutableStateOf("") }
    var tipInput by remember { mutableStateOf("") }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    //amountInput原本是放在EditNumberField里的，但是现在放到了TipTimeLayout里
    //这么做是为了共享数据，因为输入框和下方的显示部位都需要显示数据
    //当EditNumberField拥有amountInput时，只能输入框显示数据
    //放到TipTimeLayout的行为叫做hoisting,把状态从子组件提升到父组件
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0

    val tip = calculateTip(amount, tipPercent)
    //重点是把已经转成数字的amount,放到这里的calculateTip函数，得到tip
    //然后这个tip，放到下面的Text里
    //如果参数里不写tipPercent，那么不需要填入tip percentage，下面的小费也会自动显示，只是默认15%

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 40.dp)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start)
        )
        //EditNumberField(modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth())

        EditNumberField2(
            label = R.string.bill_amount,
            value = amountInput,
            onValueChange = { amountInput = it },
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )

        //我们服用了之前写好的函数，这个函数就是展示一个text field。
        //因为要多次用，展示不同的输入框，所以label不写死
        EditNumberField2(
            label = R.string.how_was_the_service,
            value = tipInput,
            onValueChange = { tipInput = it },
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.tip_amount, tip), //这里展示要付的小费
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(150.dp))
    }
}


//下面这个是有状态的函数
@Composable
fun EditNumberField(
    modifier: Modifier = Modifier) {
    var amountInput by remember { mutableStateOf("") }
    //jetpack compose必须使用remember，不然每次函数被重新调用时，变量会重新初始化
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    //toDoubleOrNull是把string转为double，如果用户输入的是文字无法转换，那么就转成null
    //后面，？：0.0,是指，如果前面那个数字不是null，那么用这个数字自身。如果是null,那么用0.0

    val tip = calculateTip(amount)

    TextField(
        label = { Text(stringResource(R.string.bill_amount)) },
        singleLine = true, //这行代码就是让textfield永远保持单行，无论用户输入多少东西
        //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        value = amountInput,
        onValueChange = { amountInput = it }, //这里的it是指用户在textfield里输入的东西
        modifier = modifier
    )
}


//下面这个是无状态的函数，也就是自己不去记变量的变化，纯靠外部。这个app用的是无状态的
//因为我们把TextField 封装成一个复用函数（ EditNumberField2()），所以label不能写死，它要支持多种输入框
//因此，这里加了一个label参数，能更灵活地使用不同label
@Composable
fun EditNumberField2 (
    @StringRes label: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
    ){
    TextField(
        label = { Text(stringResource(label)) },
        //label = { Text(stringResource(R.string.bill_amount)) },  这里是写死的，只会显示bill amount这个label
        value = value,
        onValueChange = onValueChange,
        // Rest of the code
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

private fun calculateTip(amount: Double, tipPercent: Double = 15.0): String {
    val tip = tipPercent / 100 * amount
    return NumberFormat.getCurrencyInstance().format(tip)
}




/*
To summarize, you hoisted the amountInput state from the EditNumberField()
into the TipTimeLayout() composable. For the text box to work as before,
you have to pass in two arguments to the EditNumberField() composable
function: the amountInput value and the lambda callback that updates the
amountInput value from the user's input. These changes let you calculate
the tip from the amountInput property in the TipTimeLayout() to display it
to the user.
 */
