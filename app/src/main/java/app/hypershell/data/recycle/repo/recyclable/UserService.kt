package app.hypershell.data.recycle.repo.recyclable

import app.hypershell.IUserService
import java.io.Closeable

interface UserService : Closeable {
    val privileged: IUserService
}
