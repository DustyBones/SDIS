# SDIS

Usage:

-Executable takes <MC ip> <MC port> <MCB ip> <MCB port> <MCR ip> <MCR port> as arguments

-Execution without arguments provided will use default values
    MC ip   - 225.0.0.10
    MC port - 9001
    MC ip   - 225.0.0.10
    MC port - 9002
    MC ip   - 225.0.0.10
    MC port - 9003

-Possible commands
    backup <filename or full path> <replication>
        to backup file from given path, chunks will be stored on the same location regardless
    restore <filename or full path>
        to restore file to given path
    delete <filename>
        to delete all chunks of the given file
    reclaim
        to free space by deleting chunks with higher replication rate than the minimum requested
    exit
        to exit
