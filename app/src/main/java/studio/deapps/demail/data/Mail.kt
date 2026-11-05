package studio.deapps.demail.data

data class Mail(
    val id: String,
    val sender: String,
    val subject: String,
    val body: String,
    val time: String,
    val unread: Boolean,
)

/** Used only by @Preview functions. */
object SampleData {
    val mails: List<Mail> = listOf(
        Mail("1", "Google", "Security alert for your account",
            "A new sign-in was detected on a Pixel device. If this was you, you don't need to do anything. If not, we'll help you secure your account.",
            "09:41", true),
        Mail("2", "Marko Petrović", "Re: Offer for the website redesign",
            "Hi Dejan, thanks for sending the draft. We went through it as a team and everything looks great, just a few small notes about the footer.",
            "08:15", true),
        Mail("3", "Microsoft Partner Center", "Your submission has been certified",
            "Congratulations! Your app submission passed certification and will be available in the Microsoft Store within the next 24 hours.",
            "Yesterday", false),
        Mail("4", "GitHub", "[deapps-studio/demail] New pull request",
            "A contributor opened a new pull request that updates the inbox list animation and fixes the selection state on rotation.",
            "Yesterday", true),
        Mail("5", "Ana Jovanović", "Lunch on Friday?",
            "Hey! Are you free on Friday around 13h? There's a new place near the center that I'd love to try, they say the pasta is amazing.",
            "Mon", false),
        Mail("6", "Namecheap", "Your domain renews in 14 days",
            "This is a friendly reminder that your domain is set to renew automatically. You can review your billing details in your account.",
            "Mon", false),
    )
}
