package com.nhnent.exam.netlibrary;

/**
 * Created by gradler on 17/08/2017.
 */

public interface ErrorCode {
    int NO_ERROR = 0;

    int INVALID_PARAMS = 1000;
    int INVALID_URL = 1001;
    int INVALID_METHOD = 1002;

    int OPEN_CONNECTION_FAILED = 1100;
    int IO_EXCEPTION = 9000;
}
