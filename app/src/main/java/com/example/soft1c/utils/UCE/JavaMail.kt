package com.example.soft1c.utils.UCE

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class JavaMailAPI(
    private val context: Context,
    private val email: String,
    private val mSubject: String,
    private val message: String
) {

    fun sendMail() {
        // Launch the email sending process in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val properties = Properties().apply {
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.ssl.trust", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.ssl.protocols", "TLSv1.2")
                }

                val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(MailData.EMAIL, MailData.PASSWORD)
                    }
                })

                val mimeMessage = MimeMessage(session).apply {
                    setFrom(InternetAddress(MailData.EMAIL))
                    addRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    subject = mSubject
                    setText(message)
                }

                // Send the email
                Transport.send(mimeMessage)

                // Notify on success (optional, requires switching to Main thread)
                withContext(Dispatchers.Main) {
                    // Show a toast or update UI if necessary
                    Toast.makeText(context, "Email sent successfully", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()

                // Notify on failure (optional, requires switching to Main thread)
                withContext(Dispatchers.Main) {
                    // Show an error message or update UI if necessary
                    println("Failed to send email: ${e.message}")
                }
            }
        }
    }
}
