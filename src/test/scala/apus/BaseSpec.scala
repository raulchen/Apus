package apus

import org.scalatest._

/**
 * Base class for spec
 * Created by Hao Chen on 2014/12/1.
 */
abstract class BaseSpec extends FlatSpec
  with Matchers
  with Inspectors
  with Inside
  with OptionValues
