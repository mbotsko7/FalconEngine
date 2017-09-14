/**
 TokenStreams read tokens one at a time from a stream of input.
 */
public interface TokenStream {
   /**
    Returns the next token from the stream, or null if there is no token
    available.
    */
   String nextToken();

   /**
    Returns true if the stream has tokens remaining.
    */
   boolean hasNextToken();
}
