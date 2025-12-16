package com.example.service;

import com.example.domain.PayOrder;

public interface AlipayService {
    String apply( PayOrder one,String returnUrl );
}
