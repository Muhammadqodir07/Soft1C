package com.example.soft1c.fragment

import android.bluetooth.BluetoothAdapter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.text.TextPaint
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.soft1c.R
import com.example.soft1c.databinding.ActivityPrinterBinding
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.viewmodel.AcceptanceViewModel
import com.example.tscdll.TSCActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PrinterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrinterBinding
    private lateinit var acceptance: Acceptance
    private val viewModel: AcceptanceViewModel by viewModels()
    private var date: String? = null
    private lateinit var barcodeBitmap: Bitmap
    private lateinit var barcodeInput: String
    private var macAddress: String? = null
    private val TscDll = TSCActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        acceptance = AcceptanceFragment.ACCEPTANCE
        val intent = intent
        date = convertDate(acceptance.date)
        with(binding) {
            etxtPageCount.setText(acceptance.countSeat.toString())
            txtBarcodeNumber.text =
                (getString(R.string.txt_number) + ": " + acceptance.number)
                    ?: "No data available"
            txtCurrentDate.text =
                (getString(R.string.text_date) + ": " + date)
                    ?: "No data available"
            txtClientNumber.text =
                (getString(R.string.text_code) + ": " + acceptance.client.code)
                    ?: "No data available"
            txtBarcodeSeats.text =
                (getString(R.string.text_seats) + ": " + acceptance.countSeat.toString())
                    ?: "No data available"
            macAddress = intent.getStringExtra("macAddress")
            generateBarcode()
            val view = findViewById<LinearLayout>(R.id.linear_for_print)
            view.setBackgroundColor(Color.WHITE)

            btnChoose.setOnClickListener { filePickerLauncher.launch("text/*") }
            btnPrint.setOnClickListener {
                if (macAddress != null) {
                    requestPermissions(
                        arrayOf(
                            "android.permission.BLUETOOTH_ADMIN",
                            "android.permission.BLUETOOTH_SCAN",
                            "android.permission.BLUETOOTH_CONNECT"
                        ), 80
                    )
                    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
                    if ((bluetoothAdapter == null) || !bluetoothAdapter.isEnabled) {
                        Toast.makeText(
                            this@PrinterActivity,
                            "Bluetooth is not turned on",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (etxtPageCount.text!!.isEmpty())
                            etxtPageCount.setText("1")
                        doPhotoPrint()
                    }
                } else {
                    Toast.makeText(
                        this@PrinterActivity,
                        "Mac-address is not configured",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            btnClose.setOnClickListener { closeActivity() }
        }
    }

    private fun generateBarcode() {
        barcodeInput = acceptance.number
        val writer = MultiFormatWriter()
        try {
            val matrix = writer.encode(barcodeInput, BarcodeFormat.CODE_128, 4000, 1000)

            barcodeBitmap =
                Bitmap.createBitmap(matrix.width, matrix.height + 400, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(barcodeBitmap)

            val encoder = BarcodeEncoder()
            val barcodeImage = encoder.createBitmap(matrix)
            canvas.drawBitmap(barcodeImage, 0f, 0f, null)

            val paint = TextPaint()
            paint.color = Color.BLACK
            paint.textSize = 300f
            paint.textAlign = Paint.Align.LEFT

            val spacedBarcodeInput = barcodeInput.toCharArray().joinToString(separator = "  ")
            val textBounds = Rect()
            paint.getTextBounds(spacedBarcodeInput, 0, spacedBarcodeInput.length, textBounds)
            val textWidth = textBounds.width()
            val textX = (barcodeBitmap.width - textWidth) / 2f
            canvas.drawText(spacedBarcodeInput, textX, barcodeBitmap.height - 30f, paint)

            binding.barcodeImage.setImageBitmap(barcodeBitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun closeActivity() {
        onBackPressed()
    }

    private fun doPhotoPrint() {
        Thread {
            try {
                Looper.prepare()
                TscDll.openport(macAddress)
                TscDll.setup(72, 50, 4, 14, 0, 0, 0)
                TscDll.clearbuffer()
                TscDll.sendcommand("SIZE 72 mm,50 mm\r\n")
                TscDll.sendcommand("GAP 3 mm,0\r\n")
                TscDll.sendcommand("DIRECTION 0,0\r\n")
                TscDll.sendcommand("REFERENCE 0,0\r\n")
                TscDll.sendcommand("OFFSET 0 mm\r\n")
                TscDll.sendcommand("SET PEEL OFF\r\n")
                TscDll.sendcommand("SET CUTTER OFF\r\n")
                TscDll.sendcommand("SET PARTIAL_CUTTER OFF\r\n")
                TscDll.sendcommand("SET TEAR ON\r\n")
                TscDll.sendcommand("BARCODE 561,170,\"128M\",60,0,180,3,6,\"${barcodeInput}\"\r\n")
                TscDll.sendcommand("CODEPAGE 1251\r\n")
                TscDll.sendcommand("TEXT 563,105,\"ROMAN.TTF\",180,1,28,\"${acceptance.number}\"\r\n")
                TscDll.sendcommand("TEXT 564,368,\"0\",180,74,53,\"${acceptance.client.code}\"\r\n")
                TscDll.sendcommand("TEXT 129,160,\"ROMAN.TTF\",180,1,12,\"SEATS:\"\r\n")
                TscDll.sendcommand("TEXT 130,111,\"0\",180,20,20,\"${acceptance.countSeat}\"\r\n")
                TscDll.sendcommand("TEXT 508,232,\"0\",180,12,13,\"DATE:\"\r\n")
                TscDll.sendcommand("TEXT 405,231,\"0\",180,25,12,\"${date}\"\r\n")
                TscDll.printlabel(binding.etxtPageCount.text.toString().toInt(), 1)
                Thread.sleep(500)
                TscDll.closeport()
                Looper.myLooper()?.quit()
                acceptance.type = 2
                createUpdateAcceptance()
            } catch (e: Exception) {
                runOnUiThread {
                    binding.testTV.text = e.message
                }
                e.printStackTrace()
            }
        }.start()
    }

    private fun createUpdateAcceptance() {
        viewModel.createUpdateAcceptance(acceptance)
    }

//val zplData =  """
//^XA
//^FO75,25^ADN,20,10^FDDoc: ${intent.getStringExtra("docNumber")}^FS
//^FO300,25^ADN,20,10^FDDate: ${LocalDate.now().format(dateTimeFormatter)}^FS
//^BY3,1,100
//^FO80,60^BCN,100,Y,N^FD${barcodeInput}^FS
//^FO75,200^ADN,20,10^FDCode: ${intent.getStringExtra("clientNumber")}^FS
//^FO300,200^ADN,20,10^FDSeats: ${intent.getStringExtra("seatsNumber")}^FS
//^MD30
//^XZ
//"""

    private var filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val inputStream = contentResolver.openInputStream(uri)
                val text = inputStream?.bufferedReader()?.readText()
                if (text != null) {
                    binding.testTV.setText(text)
                }
            }
        }

    fun convertDate(date: String?): String {
        return if (date != null) {
            val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

            val dateTime = LocalDateTime.parse(date)

            val formattedDateTime = dateTime.format(outputFormatter)
            formattedDateTime
        } else {
            ""
        }
    }
}