package com.example.foodapp.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.example.foodapp.ViewModel.UserViewModel
import com.example.foodapp.databinding.ActivityUserLoginBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.Identity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import com.example.foodapp.Domain.CustomerModel
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase


class UserLoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserLoginBinding
    private lateinit var viewModel: UserViewModel  // Khai báo viewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 100



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = UserViewModel()
        binding= ActivityUserLoginBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false) // cho phép chọn tài khoản mới
                    .build()
            )
            .setAutoSelectEnabled(true) // nếu tài khoản từng đăng nhập, tự động chọn
            .build()


//        var gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        googleSignInClient= GoogleSignIn.getClient(this,gso)

        setContentView(binding.root)

        setupViewBinding()

        setupLoginClick()
        setupSignUpClick()
        setupGoogleSignInClick()


    }

    private fun setupViewBinding() {
        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

//    private fun setupViewModel() {
//        viewModel = UserViewModel()
//    }

    private fun setupLoginClick() {
        binding.loginBtn.setOnClickListener {
            val email = binding.emailTxt.text.toString().trim()
            val password = binding.passTxt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            viewModel.loginUser(email, password) { success ->
                if (success) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setupSignUpClick() {
        binding.signBtn.setOnClickListener {

            val intent = Intent(this, UserSignUpActivity::class.java)
            startActivity(intent)
        }
    }
    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

//    private fun setupGoogleSignInClick() {
//        binding.btnGoogle.setOnClickListener {
//            signInWithGoogle()
//        }
//    }
private fun setupGoogleSignInClick() {
    binding.btnGoogle.setOnClickListener {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Log.e("OneTap", "Không thể khởi chạy One Tap", e)
                }
            }
            .addOnFailureListener(this) { e ->
                Log.e("OneTap", "Không thể bắt đầu One Tap: ${e.message}")
                showToast(this, "Không thể đăng nhập One Tap")
            }
    }
}
    private val oneTapLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val credential: SignInCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    val account = credential
                                    val database = FirebaseDatabase.getInstance()
                                    val ref = database.getReference("GoogleUser")

                                    val customer = CustomerModel(
                                        nameCustomer = account.displayName,
                                        emailCustomer = account.id,
                                        phoneNumberCustomer = user.phoneNumber,
                                        addressCustomer = null
                                    )

                                    ref.child(user.uid).setValue(customer)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Lỗi lưu thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } catch (e: Exception) {
                Log.e("OneTap", "Lỗi xử lý One Tap", e)
            }
        }
    }



//    private fun signInWithGoogle()
////    {
////        val signInIntent = googleSignInClient.signInIntent
////        launcher.launch(signInIntent)
////    }

//    private val launcher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
//        { result ->
//            if (result.resultCode == RESULT_OK){
//            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            handleResults(task)
//        }
//    }
//
//    private fun handleResults(task: Task<GoogleSignInAccount>) {
//        if (task.isSuccessful){
//            val account:GoogleSignInAccount?= task.result
//            if (account!=null){
//                updateUI(account)
//            }
//        }
//        else
//        {
//            showToast(this,"Sign In failed,Try again late")
//        }
//    }
//
//    private fun updateUI(account: GoogleSignInAccount) {
//
//        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
//        auth.signInWithCredential(credential).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val user = auth.currentUser
//                if (user != null) {
//                    val database = FirebaseDatabase.getInstance()
//                    val ref = database.getReference("GoogleUser")  // Node "GoogleUser"
//
//                    // Tạo đối tượng CustomerModel từ thông tin Google Account
//                    val customer = CustomerModel(
//                        nameCustomer = account.displayName,
//                        emailCustomer = account.email,
////                        profileImage = account.photoUrl?.toString(),
//                        phoneNumberCustomer = user.phoneNumber,
//                        addressCustomer = null
//                    )
//
//                    // Ghi dữ liệu vào Realtime Database tại path: GoogleUser/{user.uid}
//                    ref.child(user.uid).setValue(customer)
//                        .addOnSuccessListener {
//                            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
//                            startActivity(Intent(this, MainActivity::class.java))
//                            finish()
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(this, "Lỗi lưu thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
//                        }
//                }
//            } else {
//                Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

}