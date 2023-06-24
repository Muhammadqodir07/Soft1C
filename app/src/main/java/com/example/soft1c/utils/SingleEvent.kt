package com.example.soft1c.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
    //Pending -> рассматриваемый
    private val mPending: AtomicBoolean = AtomicBoolean(false)

    //Наблюдатель
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {

        super.observe(owner) { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    @MainThread
    override fun setValue(@org.jetbrains.annotations.Nullable t: T) {
        mPending.set(true)
        super.setValue(t)
    }
}