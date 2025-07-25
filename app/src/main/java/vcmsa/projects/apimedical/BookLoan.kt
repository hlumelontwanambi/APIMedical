package vcmsa.projects.apimedical

class BookLoan {

    data class Loan (
        val loanID: Int,
        val amount: String,
        val memberID: String,
        val message: String
        )

    data class LoanPost(
        val Amount: String,
        val memberID: String,
        val Message: String
    )
}