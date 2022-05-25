package com.kmit.findafriend.utils;

public interface Utils {
    // Add your Face endpoint to your variables.
    String apiEndpoint = "https://eastus.api.cognitive.microsoft.com/face/v1.0/";//System.getenv("FACE_ENDPOINT");
    // Add your Face subscription key to your variables.
    String subscriptionKey = "2794fd146c6e4b6b851bf6d15d5ec305";//System.getenv("FACE_SUBSCRIPTION_KEY");
    String[] suggestedMessages = {"Did you read [that book]?","Do you watch Hindi films?","I like swimming","Who is your favourite cricketer?","Have you been to [that place]?","can we chat","what varieties of dishes you cook?","Which team is your favourite in IPL?"};

}
