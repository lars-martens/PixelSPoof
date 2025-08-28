cmake_minimum_required(VERSION 3.10.2)
project("pixelspoofer")

add_library(pixelspoofer SHARED pixelspoofer.cpp)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(pixelspoofer ${log-lib} ${android-lib})

set_target_properties(pixelspoofer PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)
