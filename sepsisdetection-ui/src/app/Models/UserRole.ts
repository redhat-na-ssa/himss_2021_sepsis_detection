export interface UserRole
{
    id :  number,
    name : string,
    role : string,
    userid : string,
    password : string
}

export interface Credentials
{
    userid : string,
    password : string
}

export interface CityLocation
{
    city : string,
    state : string,
    lat: number,
    lon: number
}