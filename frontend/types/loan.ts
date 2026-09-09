export type CreateLoanRequest = {
  bookId: number;
};

export type LoanResponse = {
  id: number;
  bookTitle: string;
  borrowerEmail: string;
  loanDate: string;
  dueDate: string;
  returnDate: string | null;
};
