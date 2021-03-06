/* 
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package geb.waiting

/**
 * Represents a particular configuration of waiting, but does not encompass what is to be waited on.
 * <p>
 * Generally not used by user code, but used internally by {@link geb.conf.Configuration} and {@link geb.waiting.WaitingSupport}.
 * 
 * @see #waitFor(Closure)
 */
class Wait {
	
	/**
	 * 5 seconds
	 */
	static public final Double DEFAULT_TIMEOUT = 5
	
	/**
	 * 100 milliseconds
	 */
	static public final Double DEFAULT_RETRY_INTERVAL = 0.1
	
	/**
	 * The maximum amount of seconds that something can be waited on.
	 */
	final Double timeout
	
	/**
	 * How many seconds to wait before trying something again while waiting.
	 */
	final Double retryInterval
	
	
	Wait(Double timeout = DEFAULT_TIMEOUT, Double retryInterval = DEFAULT_RETRY_INTERVAL) {
		this.timeout = timeout
		this.retryInterval = [timeout, retryInterval].min()
	}

	String toString() {
		"Wait[timeout: $timeout, retryInterval: $retryInterval]"
	}
	
	boolean equals(other) {
		if (this.is(other)) {
			true
		} else if (!other instanceof Wait) {
			false
		} else {
			this.timeout == other.timeout && this.retryInterval == other.retryInterval
		}
	}
	
	int hashCode() {
		int code = 41
		code = 31 * code + timeout.hashCode()
		code = 31 * code + retryInterval.hashCode()
		code
	}
	
	/**
	 * Invokes the given {@code block} every {@code retryInterval} seconds until it returns
	 * a true value according to the Groovy Truth. If {@code block} does not return a truish value
	 * within {@code timeout} seconds then a {@link geb.waiting.WaitTimeoutException} will be thrown.
	 * <p>
	 * If {@code block} throws an exception, it is treated as a failure and the {@code block} will be tried
	 * again after the {@code retryInterval} has expired. If the last invocation of {@code block} throws an exception
	 * it will be the <em>cause</em> of the {@link geb.waiting.WaitTimeoutException} that will be thrown.
	 */
	def waitFor(Closure block) {
		def numTries = Math.ceil(timeout / retryInterval)
		def pass
		def thrown
		
		try {
			pass = block()
		} catch (Throwable e) {
			pass = false
			thrown = e
		}
		
		def i = 0
		while (!pass && i++ < numTries) {
			sleepForRetryInterval()
			try {
				pass = block()
				thrown = null
			} catch (Throwable e) {
				pass = false
				thrown = e
			}
		}
		
		if (i >= numTries) {
			throw new WaitTimeoutException(this, thrown)
		}
		
		pass
	}
	
	/**
	 * Blocks the caller for the retryInterval
	 */
	void sleepForRetryInterval() {
		Thread.sleep((retryInterval * 1000) as long)
	}
}