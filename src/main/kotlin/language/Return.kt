package language

import java.lang.RuntimeException

/**
 * 其中那个奇怪的带有`null`和`false`的父类构造器方法，禁用了一些我们不需要的JVM机制。因为我们只是使用该异常类来控制流，而不是真正的错误处理，所以我们不需要像堆栈跟踪这样的开销。
 * @property value Any
 * @constructor
 */
class Return(val value:Any?): RuntimeException(null,null,false,false) {

}