package com.demo.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import com.demo.geode.model.BookGemfire;
import com.demo.geode.repository.BookRepositoryGemfire;
import com.demo.mysql.model.BookMysql;
import com.demo.mysql.repository.BookRepositoryMysql;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.*;

@RestController

public class Controller {

    @Autowired
    private BookRepositoryMysql bookRepositoryMysql;

    @Autowired
    private BookRepositoryGemfire bookRepositoryGemfire;

    @Value("${datasource}")
    private String datasource;

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @RequestMapping(value = {"/**","/*"},method = RequestMethod.GET)
    public void noPageFind(HttpServletRequest request,HttpServletResponse response) throws IOException {
        response.getWriter().println("no page found: "+ request.getRequestURI()+" is not a vaild url!");
        System.out.println("error: no page found: "+ request.getRequestURI()+" is not a vaild url!");
    }

    @ExceptionHandler(Exception.class)
    public void processMethod(Exception ex, HttpServletRequest request , HttpServletResponse response) throws IOException {
        System.out.println("error: "+ex.getMessage());
        response.getWriter().printf("error: "+ex.getMessage());
        response.flushBuffer();
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String addBook(@RequestParam(value = "id", required = false,defaultValue = "") String id,
                          @RequestParam(value = "isbn", required = true) String isbn,
                          @RequestParam(value = "press", required = false,defaultValue = "") String press,
                          @RequestParam(value = "user", required = false,defaultValue = "") String user,
                          @RequestParam(value = "loantime", required = false,defaultValue = "") String loantime,
                          @RequestParam(value = "bookname", required = true) String bookname,
                          @RequestParam(value = "category", required = false,defaultValue = "") String category,
                          @RequestParam(value = "returntime", required = false,defaultValue = "") String returntime,
                          HttpServletResponse response) throws IOException {
        switch (datasource) {
            case "Gemfire":
                if (id.equals("")){
                    response.getWriter().println("Error: in Gemfire model, you must input id!");
                    return null;
                }
                BookGemfire bookGemfire = new BookGemfire(id, bookname, isbn, category, press, user, loantime, returntime, df.format(new Date()));
                bookRepositoryGemfire.save(bookGemfire);
                return bookGemfire.toString();
            case "Mysql":
                BookMysql bookMysql = new BookMysql(isbn, bookname, press, category, new Date(), "", "", "");
                bookRepositoryMysql.save(bookMysql);
                if (!id.equals("")){
                    response.getWriter().println("Warning: in Mysql model, your input id will not work, it will be automatic assigned !");
                    response.getWriter().println("<br/>");
                    response.getWriter().println(bookMysql.toString());
//                    response.flushBuffer();
                }else{
                    return bookMysql.toString();
                }
            case "File":
                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                return null;
        }
    }

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public Object deleteBook(@RequestParam(value = "isbn", required = true) String isbn,
                             HttpServletResponse response) throws IOException {
        switch (datasource){
            case "Gemfire":
                Collection<BookGemfire> result =bookRepositoryGemfire.findByIsbn(isbn);
                for (BookGemfire element:result){
                    bookRepositoryGemfire.deleteById(element.getId());
                }
                return result;
            case "Mysql":
                return bookRepositoryMysql.deleteByIsbnEquals(isbn);
            case "File":
                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                return null;
        }
    }

    @RequestMapping(value = "/select/bookname/{bookname}", method = RequestMethod.GET)
    public Object findBook(@PathVariable(required = true) String bookname,
                           HttpServletResponse response) throws IOException {
        switch (datasource){
            case "Gemfire":
                return bookRepositoryGemfire.findByBooknameLike("%"+bookname+"%");
            case "Mysql":
                return bookRepositoryMysql.findByBooknameContains(bookname);
            case "File":
                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                return null;
        }
    }

    @RequestMapping(value = "/select/user/{user}", method = RequestMethod.GET)
    public Object findUser(@PathVariable(required = true) String user,
                           HttpServletResponse response) throws IOException {
        switch (datasource){
            case "Gemfire":
                return bookRepositoryGemfire.findByUser(user);
            case "Mysql":
                return bookRepositoryMysql.findByUserEquals(user);
            case "File":
                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                return null;
        }
    }

    @RequestMapping(value = "/describe", method = RequestMethod.GET)
    public  Object describeBook(
            @RequestParam(value = "start", required = false,defaultValue = "0") String start,
            @RequestParam(value = "nums", required = false,defaultValue = "all") String nums,
            HttpServletResponse response) throws IOException {
        switch (datasource){
            case "Gemfire":
                int num1 = (nums.equals("all"))?-1:Integer.valueOf(nums);
                return  bookRepositoryGemfire.describe(num1);
            case "Mysql":
                int starts = Integer.valueOf(start);
                int num2 = (nums.equals("all"))?bookRepositoryMysql.countAll():Integer.valueOf(nums);
                return bookRepositoryMysql.describe(starts, num2);
            case "File":
                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                return null;
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public Object updateBook(@RequestParam(value = "press", required = false) String press,
                          @RequestParam(value = "category", required = false,defaultValue = "") String category,
                          @RequestParam(value = "bookname", required = false) String bookname,
                          @RequestParam(value = "isbn", required = false) String isbn,
                          @RequestParam(value = "condition", required = true) String condition,
                            HttpServletResponse response) throws IOException {
            if (!StringUtils.isBlank(isbn) && ((isbn.equals("''") | (isbn.equals("\"\""))))) {
                response.getWriter().println("don't set the isbn to null");
                return null;
            } else {
                String datetime = df.format(new Date());
                switch (datasource) {
                    case "Gemfire":
                        Collection<BookGemfire> result = bookRepositoryGemfire.findByIsbn(condition);
                        if (!StringUtils.isBlank(bookname))
                            for (BookGemfire element : result) {
                                element.setBookname((bookname.equals("''") | (bookname.equals("\"\""))) ? "" : bookname);
                                element.setUpdatetime(datetime);
                            }
                        if (!StringUtils.isBlank(press))
                            for (BookGemfire element : result) {
                                element.setPress((press.equals("''") | (press.equals("\"\""))) ? "" : press);
                                element.setUpdatetime(datetime);
                            }
                        if (!StringUtils.isBlank(category))
                            for (BookGemfire element : result) {
                                element.setCategory((category.equals("''") | (category.equals("\"\""))) ? "" : category);
                                element.setUpdatetime(datetime);
                            }
                        if (!StringUtils.isBlank(isbn))
                            for (BookGemfire element : result) {
                                element.setIsbn(isbn);
                                element.setUpdatetime(datetime);
                            }
                        bookRepositoryGemfire.saveAll(result);
                        return result;

                    case "Mysql":
                        if (!StringUtils.isBlank(bookname))
                            bookRepositoryMysql.updateBookname((bookname.equals("''") | (bookname.equals("\"\""))) ? "" : bookname, datetime, condition);
                        if (!StringUtils.isBlank(category))
                            bookRepositoryMysql.updateCategory((category.equals("''") | (category.equals("\"\""))) ? "" : category, datetime, condition);
                        if (!StringUtils.isBlank(press))
                            bookRepositoryMysql.updatePress((press.equals("''") | (press.equals("\"\""))) ? "" : press, datetime, condition);
                        if (!StringUtils.isBlank(isbn)) bookRepositoryMysql.updateIsbn(isbn, datetime, condition);
                        String tmpisbn = (StringUtils.isBlank(isbn)) ? condition : isbn;
                        return bookRepositoryMysql.selectByIsbn(tmpisbn);

                    case "File":
                        return null;
                    default:
                        response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                        return null;
            }
        }
    }

    @RequestMapping(value = "/loanbook", method = RequestMethod.GET)
    public void loanBook(@RequestParam(value = "isbn", required = true) String isbn,
                           @RequestParam(value = "user", required = true) String user,
                           HttpServletResponse response) throws ParseException, IOException {
        switch (datasource){
            case "Gemfire":
                boolean tag=false;
                Collection<BookGemfire> result = bookRepositoryGemfire.findByIsbn(isbn);
                for (BookGemfire element : result) {
                    if (element.getLoantime().equals("")) {
                        element.setLoantime(df.format(new Date()));
                        element.setUser(user);
                        bookRepositoryGemfire.save(element);
                        response.getWriter().println(user+" success borrowing isbn: s"+ isbn +" at "+df.format(new Date()));
                        tag=true;
                        break;
                    } else if (!element.getReturntime().equals("") && df.parse(element.getReturntime()).after(df.parse(element.getLoantime()))) {
                        element.setLoantime(df.format(new Date()));
                        element.setUser(user);
                        bookRepositoryGemfire.save(element);
                        response.getWriter().println(user+" success borrowing isbn: s"+ isbn +" at "+df.format(new Date()));
                        tag=true;
                        break;
                    }
                }
                if (tag) break;
                response.getWriter().println("no book remained");
                break;

            case "Mysql":
                Collection<BookMysql> res = bookRepositoryMysql.serachNoloanedBookByIsbn(isbn);
                if (res.isEmpty()){
                        response.getWriter().println("no book remained");
                }else{
                    Long id =res.iterator().next().getId();
                    bookRepositoryMysql.loanBook(user,df.format(new Date()),id);
                    response.getWriter().println(user+" success borrowing isbn: s"+ isbn +" at "+df.format(new Date()));
                }
                break;
            case "File":
                break;
//                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
//                return null;
        }
    }

    @RequestMapping(value = "/returnbook", method = RequestMethod.GET)
    public Object returnBook(@RequestParam(value = "isbn", required = true) String isbn,
                                              @RequestParam(value = "user", required = true) String user,
                                              HttpServletResponse response) throws ParseException, IOException {
        switch (datasource){
            case "Gemfire":
                Collection<BookGemfire> result =bookRepositoryGemfire.describe(-1);
                if (!result.isEmpty()) {
                    result = result.stream().filter(book -> book.getIsbn().equals(isbn) & book.getUser().equals(user)).collect(Collectors.toList());
                    BookGemfire temp = result.iterator().next();
                    temp.setUser(user);
                    temp.setReturntime(df.format(new Date()));
                    bookRepositoryGemfire.save(temp);
                    response.getWriter().println(user + " success returning isbn: " + isbn + " at " + df.format(new Date()));
                }else
                    response.getWriter().println("you have not borrowed the book");
                return null;

            case "Mysql":
                Collection<BookMysql> res = bookRepositoryMysql.serachloanedBookByBooknameAndIsbn(user,isbn);
                if (res.isEmpty()){
                    response.getWriter().println("you have not borrowed the book");
                }else{
                    bookRepositoryMysql.returnBook(res.iterator().next().getId());
                    response.getWriter().println(user+" success returning isbn: "+ isbn +" at "+ df.format(new Date()));
                }
                return null;
            case "File":
                return null;
            default:
                response.getWriter().println("please choose the vaild database,it support Gemife/Mysql/File!");
                return null;
        }


}






















}