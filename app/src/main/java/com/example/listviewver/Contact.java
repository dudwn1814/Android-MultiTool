package com.example.listviewver;

class Contact{
    private String Name;
    private String PhoneNumber;

    public Contact(String _Name, String _PhoneNumber)
    {
        this.Name = _Name;
        this.PhoneNumber = _PhoneNumber;
    }

    public String getName() {
        return Name;
    }

    public String getNumber() {
        return PhoneNumber;
    }
}
