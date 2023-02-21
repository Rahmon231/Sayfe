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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSignUpBinding
import com.lemzeeyyy.sayfe.model.Users

class SignUpFragment : Fragment() {

    private lateinit var fAuth: FirebaseAuth
    private var _binding:FragmentSignUpBinding? =  null
    private val binding get() = _binding!!
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Users")
    private lateinit var progressBar: ProgressBar
    private var appToken = ""

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
           findNavController().navigate(R.id.signInFragment)
        }



    }

    private fun createUserEmailAndPassword(
        emailString: String,
        passwordString: String,
        fullName: String){
        fAuth.createUserWithEmailAndPassword(emailString, passwordString)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.progressSignup.visibility = View.GONE
                    Toast.makeText(requireContext(),"Account created successfully",Toast.LENGTH_LONG).show()
                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { tokenTask ->
                        if (!tokenTask.isSuccessful) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }

                        // Get new FCM registration token
                        appToken = tokenTask.result

                        val user = fAuth.currentUser
                        val currentUserId = user!!.uid

                        val users = Users(phoneNumber = "",appToken,currentUserId,fullName)
                        collectionReference.add(users)
                            .addOnSuccessListener {
                                findNavController().navigate(R.id.addPhoneNumber)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(),it.toString(),Toast.LENGTH_LONG).show()
                            }
                    })

//
                } else {
                    binding.progressSignup.visibility = View.GONE
                    // If sign in fails, display a message to the user.
                    Log.w("TAGd", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed ${task}.",
                        Toast.LENGTH_SHORT).show()

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