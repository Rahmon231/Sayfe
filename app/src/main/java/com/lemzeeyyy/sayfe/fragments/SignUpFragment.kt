package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private lateinit var fAuth: FirebaseAuth
    private var _binding:FragmentSignUpBinding? =  null
    private val binding get() = _binding!!
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Users")
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = Firebase.auth

        binding.agreeAndRegisterBtnSignUp.setOnClickListener {
            binding.progressSignup.visibility = View.VISIBLE


            val emailString : String = binding.emailEtSignUp.text!!.toString()
            val passwordString : String = binding.passwordEtSignUp.text!!.toString()
            val fullName : String = binding.fullNameEtSignUp.text.toString()
            val confirmPasswordString : String = binding.confirmPasswordEtSignUp.text!!.toString()
            if(emailString.isNotEmpty() && passwordString.isNotEmpty() && confirmPasswordString.isNotEmpty() && fullName.isNotEmpty()) {
                if (passwordString == confirmPasswordString && binding.checkBoxSignUp.isChecked) {
                    createUserEmailAndPassword(
                        binding.emailEtSignUp.text!!.toString(),
                        binding.passwordEtSignUp.text!!.toString(),
                        binding.fullNameEtSignUp.text.toString()
                    )


                } else if (passwordString == confirmPasswordString && !binding.checkBoxSignUp.isChecked){
                    binding.progressSignup.visibility = View.GONE
                    binding.passwordMismatchTxtSignUp.setText("You have to agree to terms of use....")
                    binding.passwordMismatchTxtSignUp.visibility = View.VISIBLE
                }
                else{
                    binding.progressSignup.visibility = View.GONE
                    binding.passwordMismatchTxtSignUp.setText("Password Must Match")
                    binding.passwordMismatchTxtSignUp.visibility = View.VISIBLE
                }
            }
            else{
                binding.progressSignup.visibility = View.GONE
                binding.passwordMismatchTxtSignUp.visibility = View.GONE
                Toast.makeText(requireContext(),"No field should be empty",Toast.LENGTH_LONG).show()
            }

        }

        binding.signInTvSignup.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.signInFragment)
        }



    }

    private fun createUserEmailAndPassword(
        emailString: String,
        passwordString: String,
        fullName: String) {
        fAuth.createUserWithEmailAndPassword(emailString, passwordString)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.progressSignup.visibility = View.GONE
                    Navigation.findNavController(binding.signInTvSignup).navigate(R.id.signInFragment)
                    // Sign in success, go to dashboard
                    Log.d("TAG", "createUserWithEmail:success")
                    val user = fAuth.currentUser
                    val currentUserId = user!!.uid
                    val userInfo = hashMapOf(
                        "userid" to currentUserId,
                        "fullName" to fullName,
                        "phoneNumber" to ""
                    )
                    collectionReference.add(userInfo)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(),it.id.toString(),Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(),it.toString(),Toast.LENGTH_LONG).show()
                        }
//
                } else {
                    binding.progressSignup.visibility = View.GONE
                    // If sign in fails, display a message to the user.
                    Log.w("TAGd", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed ${task}.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }

            }
            .addOnFailureListener {
                Log.d("TAGd", it.message.toString())
                Toast.makeText(requireContext(),it.message.toString(),Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = fAuth.currentUser
        if(currentUser != null){
           //get signed in
        }
    }

}