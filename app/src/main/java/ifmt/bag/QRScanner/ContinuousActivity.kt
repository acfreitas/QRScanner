package ifmt.bag.evento_scanner

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Toast
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import java.util.*
import kotlinx.android.synthetic.main.activity_continuous.*
import java.io.File
import java.io.FileInputStream

class ContinuousActivity : AppCompatActivity() {

    private lateinit var captureManager: CaptureManager
    private var torchState: Boolean = false
    private var scanContinuousState: Boolean = false
    private lateinit var scanContinuousBG: Drawable
    lateinit var beepManager: BeepManager
    private var lastScan = Date()
    private var lastSave: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_continuous)

        title = "Jenpex 2022"

        captureManager = CaptureManager(this, barcodeView)
        captureManager.initializeFromIntent(intent, savedInstanceState)
        beepManager = BeepManager(this)
        beepManager.isVibrateEnabled = true
        scanContinuousBG = btnScanContinuous.background

        var callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    val current = Date()
                    val diff = current.time - lastScan.time
                    if(diff >= 1000){
                        txtResultContinuous.text = it.text
                        lastScan = current
                        beepManager.playBeepSoundAndVibrate()

                        animateBackground()
                    }
                }
            }
            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            }
        }

    btnScanContinuous.setOnClickListener(View.OnClickListener {
        if(!scanContinuousState){
            scanContinuousState = !scanContinuousState
            btnScanContinuous.setBackgroundColor(
                ContextCompat.getColor(InlineScanActivity@this, R.color.colorPrimary))
            txtResultContinuous.text = "Verificando..."
            barcodeView.decodeContinuous(callback)
        } else {
            scanContinuousState = !scanContinuousState
            btnScanContinuous.background = scanContinuousBG
            barcodeView.barcodeView.stopDecoding()
        }
    })

        btnSave.setOnClickListener(View.OnClickListener {
            val path = baseContext.getFilesDir()
            val letDirectory = File(path, "LET")
            letDirectory.mkdirs()
            val file = File(letDirectory, "records.txt")
            if (edtDescription.text.isEmpty()){
                Toast.makeText(
                    applicationContext, "Descrição não pode ser vazio!", Toast.LENGTH_LONG).show()
            }else if(lastSave.equals(txtResultContinuous.text.toString() + ", " + edtDescription.text.toString())){
                Toast.makeText(
                    applicationContext,
                    txtResultContinuous.text.toString() + " já foi salvo em " + edtDescription.text.toString(),
                    Toast.LENGTH_LONG).show()
            }else if(txtResultContinuous.text.toString() == "..." || txtResultContinuous.text.toString() == "Salvo!" || txtResultContinuous.text.toString() == "Verificando..."){
                Toast.makeText(
                    applicationContext, "Número de inscrição não digitalizado!", Toast.LENGTH_LONG).show()
            }
            else{
                lastSave = txtResultContinuous.text.toString() + ", " + edtDescription.text.toString()
            file.appendText("\n" +
                    txtResultContinuous.text.toString() + ", " + edtDescription.text.toString() + ", " + Date())
                txtResultContinuous.text = "Salvo!"
                txtResultContinuous.setBackgroundColor(
                    ContextCompat.getColor(InlineScanActivity@this, R.color.colorSave))
            }
        })

        btnShare.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")

            val path = baseContext.getFilesDir()
            val letDirectory = File(path, "LET")
            val file = File(letDirectory, "records.txt")

            val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }

            intent.putExtra(Intent.EXTRA_TEXT, inputAsString)

            startActivity(intent)
        })

        btnTorch.setOnClickListener {
            if(torchState){
                torchState = false
                barcodeView.setTorchOff()
            } else {
                torchState = true
                barcodeView.setTorchOn()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        captureManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        captureManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        captureManager.onDestroy()
    }

    private fun animateBackground(){
        val colorFrom = resources.getColor(R.color.colorAccent)
        val colorTo = resources.getColor(R.color.colorPrimary)
        val colorAnimation =
            ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 250 // milliseconds

        colorAnimation.addUpdateListener { animator ->
            txtResultContinuous.setBackgroundColor(animator.animatedValue as Int) }
        colorAnimation.start()
    }
}