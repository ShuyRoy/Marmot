cmake_minimum_required(VERSION 3.22.1)

project("llama")

add_library(
        llama
        SHARED
        llama.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        llama
        ${log-lib}
)