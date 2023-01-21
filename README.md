# wordleClient

## Description
This is a simple client that connects to the wordle server, sends guesses, and then 
prints a secret flag received in response. 

## Approach
The client connects to the server, then sends a hello message and receives a "start" response. 
At this point, the client sends a guess, and receives a "retry" or "bye" response. If the 
response is "retry", the client will send the marks received from the server, indicating the correctness
of the guess to a "GuessFactory", where it will eliminate all words from a word bank that do not match
the marks. The client will then send a new guess, and repeat the process until the server sends a "bye"
response. The "bye" response will contain the secret flag.

## Strategy and Challenges
The algorithm begins with a word bank of every possible word. When receiving a "retry" response, the
guess factory will receive the marks, and will iterate through each letter for each word in marks, and eliminate
words in the word bank that do not comply with the marks. The algorithm will then send a new guess, which is just
the first word in the word bank. 

The biggest challenge was working with words with multiple of the same letter in the GuessFactory.
The algorithm goes through each letter individually, and if the letter has a mark of 0 (indicating it's not in the word),
it would be eliminated from the word bank, even if it shows up somewhere else. 

To get around this, the algorithm will add the letter to a list representing where it cannot be in the word. 
If the letter is found with a different mark, it will be removed from that list at the correct position. 
Once the word is fully parsed through, the guess factory will remove all words that still do not comply.

## Testing
The algorithm was testing using unit tests for words, and was run many times against the server to ensure it would 
receive the secret flag every time. In addition, edge cases where the word would have double letters were also tested. 