import scala.util.control.Breaks

//
//def findLongestSubstringWithAtMost2UniqueChars(s: String): String = {
//  type Result = (Int, Int)
//  type Param = (Result, Char, Char, Int, Int, Int)
//
//  val init: Param = ((0, 0), '\0', '\0', 0, 0, 0)
//  def op(p: Param, cur: Char): Param = p match {
//    case (res, c1, c2, c1Next, c2Next, index) => {
//      def maxLen: Result = ???
//      cur match {
//        case `c1` => (maxLen, c1, c2, c1Next, c2Next, index+1)
//        case `c2` => (maxLen, c1, c2, index+1, c2Next, index+1)
//        case _ => (maxLen, c2, cur, )
//      }
//    }
//    case ((begin, end), c1, c2, c1Begin, c2Begin, index) => {
//      def maxInterval(nb: Int, ne:Int) = {
//        if(ne - nb >= begin - end) (nb, ne) else (begin, end)
//      }
//      val x = cur match {
//        case `c1` => (maxInterval(Math.min(c1Begin, c2Begin), index+1) , c2, c1, c2Begin, c1Begin, index+1)
//        case `c2` => (maxInterval(Math.min(c1Begin, c2Begin), index+1) , c1, c2, c1Begin, c2Begin, index+1)
//        case _ => (maxInterval(c2Begin, index+1), c2, cur, c2Begin, index, index+1)
//      }
//      println(x)
//      x
//    }
//  }
//
//  val (begin, end) = s.foldLeft(init)(op)._1
//  s.substring(begin, end)
//}
//
//findLongestSubstringWithAtMost2UniqueChars("abac")
//


def longest2UniqueCharSubstring(s:String) = {
  var c1, c2 = '\0'
  var n1, n2 = 0
  var cb = 0
  var b, e = 0
  for( i <- 0 to s.length ){
    val c = s(i)
    if(c == c1) {
      n1+=1
    }
    else if(c == c2){
      n2+=1
    }
    else{
      Breaks.breakable{
        while(cb < i){
          if(n1 == 0){
            c1 = c2
            Breaks.break()
          }
          else if(n2 == 0){
            Breaks.break()
          }
          s(cb) match {
            case `c1` => n1-=1
            case `c2` => n2-=1
          }
          cb+=1
        }
      }
    }

    (b, e) = if(i+1 - cb >= e - b) (cb, i+1) else (b, e)
  }
  s.substring(b, e)
}

longest2UniqueCharSubstring("abac")