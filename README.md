Echo
==============

Intelligent API for travel agents to find similar hotels with natural language queries. Bonus: A chat app demo with the API!

Problem Statement
-----------------

It is difficult for travel agents to find similar hotels to recommend to customers, moreso for international customers.

What does that mean exactly?
-----------------------------

Say you're a customer. You like a hotel you went to Singapore last year. It has a sea facing view, and a minibar in the room. But this year you're going to Korea, and you'd like a similar hotel. You tell your travel agent about it. Now the travel agent would be hard pressed to actually find all the hotels that provide all the services you want through existing apis. Even travego doesn't provide an extensive list of all the features a hotel has.

Yeah, that's a problem I've met before too! So how do you guys solve that?
----------------------------------

Well, basically we provide that API. First, we parsed through the a hotel content provider for all the descriptions, facts, addresses of the hotels to create a database of hotels. Then we trained an AI to identify parts of human speech for what people want for their hotels. After that, we use the data from the AI to look through our database, using several other NLP comparison algorithms, and return that data to the user.

TL;DR: You use an API to talk to the AI, and the AI compares and pulls data from the database for you.

That sounds pretty cool actually! So did you guys make the AI yourselves?
---------------------------------------------

Nah, we weren't that talented. We simply stood on the shoulders on giants. We used api.ai for our machine learning purposes. It has a pretty intuitive interface, so it was easy for us to train the AI without too much hassle.

I see. So which API did one you chose for this cool task?
------------------------------------------

GIATA was the hotel content provider of our choice, because it was the only API providing both multicode and multilingual capibilities. We wanted our API to be part of the travel agent workflow, so talking to other APIs easily was part of the package. Also, we want the API to be language-agnostic, so GIATA's API gelled well with our requirements. 

Anything you guys envision for the future for this?
---------------------------------------------

We hope to train the AI on the database directly instead, so we could skip running NLP algorithms on the hotel descriptions. Also we want to extend the API to other languages and countries. We currently are limiting it to Singapore because of our limited time and computing power. 

The future is simply endless when you fused machine intelligence and data together!

It may seem too late to ask this, but why the name?
-------------------------------------

Someone told me to say that it was because the AI echoes your needs or something like that, but that isn't true. I chose this name because I was browsing through /r/lolphp and saw a cute bug with the php echo function. So yeah, good luck and have fun!
