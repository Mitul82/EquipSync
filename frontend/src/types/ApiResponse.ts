interface ApiRes<T> {
    message: string,
    data?: T | null
}

export type { ApiRes }