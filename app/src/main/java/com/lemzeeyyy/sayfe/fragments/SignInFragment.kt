package com.lemzeeyyy.sayfe.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.databinding.FragmentSignInBinding


class SignInFragment : Fragment() {
    lateinit var binding : FragmentSignInBinding
    private lateinit var fAuth: FirebaseAuth
    private val database = Firebase.firestore
    private val collectionReference = database.collection("Users")



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fAuth = Firebase.auth
        binding.loginSignin.setOnClickListener {
            binding.progressSignin.visibility = View.VISIBLE
            val email = binding.emailEtSignIn.text.toString()
            val password = binding.passwordEtSignIn.text.toString()
            if(binding.rememberMeCheckBoxSignIn.isChecked){
                binding.passwordEtSignIn.setText(password)
                binding.emailEtSignIn.setText(email)
            }
            if(email.isNotEmpty() && password.isNotEmpty()) {
                signInUsers(email, password)
            }else{
                binding.progressSignin.visibility = View.GONE
                Toast.makeText(requireContext(),"No field should be empty",Toast.LENGTH_SHORT).show()
            }
        }
        binding.signUpTvSignIn.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.signUpFragment)
        }

    }

    private fun signInUsers(email: String, password: String) {
        fAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.progressSignin.visibility = View.GONE
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success")
                    Toast.makeText(requireContext(),"Success",Toast.LENGTH_SHORT).show()
                    val user = fAuth.currentUser
                    val currentUserId = user!!.uid
                    collectionReference.whereEqualTo("userid",currentUserId)
                        .addSnapshotListener { value, error ->
                            if(!value!!.isEmpty){
                                for( snapshot : QueryDocumentSnapshot in value){
                                   val phone = snapshot.get("phoneNumber")
                                    if(phone!=""){
                                        Navigation.findNavController(binding.signUpTvSignIn).navigate(R.id.dashboardFragment)
                                    }else
                                        Navigation.findNavController(binding.signUpTvSignIn).navigate(R.id.addPhoneNumber)
                                }

                            }
                        }


                    //go to dashboard
                } else {
                    // If sign in fails, display a message to the user.
                    binding.progressSignin.visibility = View.GONE
                    Log.d("TAG", "signInWithEmail:failure ${task.exception}")
                    Toast.makeText(requireContext(), "Authentication failed. ${task.exception.toString()}",
                        Toast.LENGTH_SHORT).show()

                }
            }
    }

    override fun onStart() {
        super.onStart()
//        if (fAuth.currentUser != null){
//            Navigation.findNavController(binding.signUpTvSignIn).navigate(R.id.dashboardFragment)
//        }
    }

}