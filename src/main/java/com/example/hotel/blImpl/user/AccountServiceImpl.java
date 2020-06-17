package com.example.hotel.blImpl.user;

import com.example.hotel.bl.user.AccountService;
import com.example.hotel.data.user.AccountMapper;

import com.example.hotel.po.User;
import com.example.hotel.vo.UserForm;
import com.example.hotel.vo.ResponseVO;
import com.example.hotel.vo.UserVO;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Base64;


@Service
public class AccountServiceImpl implements AccountService {
    private final static String ACCOUNT_EXIST = "账号已存在";
    private final static String UPDATE_ERROR = "修改失败";

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public ResponseVO registerAccount(UserVO userVO) {
        User user = new User();
        BeanUtils.copyProperties(userVO,user);
        try {
            accountMapper.createNewAccount(user);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseVO.buildFailure(ACCOUNT_EXIST);
        }
        return ResponseVO.buildSuccess();
    }

    @Override
    public User login(UserForm userForm) {
        User user = accountMapper.getAccountByEmail(userForm.getEmail());
        if (null == user || !user.getPassword().equals(userForm.getPassword())) {
            return null;
        }
        user.setAvatarurl(getUserImg(user.getId()));
        return user;
    }

    @Override
    public User getUserInfo(int id) {
        User user = accountMapper.getAccountById(id);
        user.setAvatarurl(getUserImg(id));
        if (user == null) {
            return null;
        }
        return user;
    }

    @Override
    public ResponseVO updateUserInfo(int id, String password, String username, String phonenumber,String avatarurl) {
        try {
            accountMapper.updateAccount(id, password, username, phonenumber,avatarurl);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseVO.buildFailure(UPDATE_ERROR);
        }
        return ResponseVO.buildSuccess(true);
    }

    public ResponseVO creditSet(UserVO userVO){
        User user = accountMapper.getAccountByEmail(userVO.getEmail());
        user.setCredit(userVO.getCredit());
        accountMapper.setCredit(user.getId(),user.getCredit());
        return ResponseVO.buildSuccess(true);
    }

    public ResponseVO lvSet(UserVO userVO){
        User user = accountMapper.getAccountByEmail(userVO.getEmail());
        user.setTotalmoney(userVO.getTotalmoney());
        user.setLv((int) ((user.getTotalmoney()<=10000)?(user.getTotalmoney()/1000):(9+user.getTotalmoney()/10000)));
        accountMapper.setLv(user.getId(),user.getLv());
        accountMapper.setTotalMoney(user.getId(),user.getTotalmoney());
        return ResponseVO.buildSuccess(true);
    }

    public User getAccountByEmail(String email) {
        User user = accountMapper.getAccountByEmail(email);
        if (user == null) {
            return null;
        }
        return user;
    }

    @Override
    public ResponseVO updateUserImg(MultipartFile file, Integer userId) {
        String fileName = file.getOriginalFilename();
        String ext = fileName.substring(fileName.lastIndexOf('.')+1);
        String newFileName = String.valueOf(userId) + ".jpg";
        String dirPath = '.'+ File.separator+"savedimgs"+File.separator+"user";
        String filePath = dirPath + File.separator + newFileName;
        File imgPath = new File(dirPath);
        if (!imgPath.exists()){
            imgPath.mkdirs();
        }
        File localFile = new File(filePath);
        try {
            file.transferTo(localFile.getAbsoluteFile());
            return ResponseVO.buildSuccess(true);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getUserImg(Integer userId) {
        String newFileName = String.valueOf(userId) + ".jpg";
        String dirPath = '.'+ File.separator+"savedimgs"+File.separator+"user";
        String filePath = dirPath + File.separator + newFileName;
        InputStream in;
        byte[] data;
        try {
            in = new FileInputStream(new File(filePath));
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (FileNotFoundException e){
            return "iVBORw0KGgoAAAANSUhEUgAAAHgAAAB4CAYAAAA5ZDbSAAAABGdBTUEAANjr9RwUqgAAACBjSFJNAACHDwAAjA0AAPmTAACE5QAAe4IAAOt1AAA/tAAAIlh1a16cAAAEDGlDQ1BJQ0MgUHJvZmlsZQAASMeNVV1oHFUUPrtzZyMkzlNsNIV0qD8NJQ2bVjShtLp/3d02bpZJNtoi6GT27s6Yycw4M7v9oU9FUHwx6psUxL+3gCAo9Q/bPrQvlQol2tQgKD60+INQ6Ium65k7M5lpurHeZe58853vnnvuuWfvBei5KluWnhQBFgzXlopZ8bnDR8SeFUjCQ9ALg9ArK46VqVYnAZunhbvare8h4b2v7Opu/8/WW6eOApC4D7FZd5QFxMcA+NOKZbsYXz/y40ddy8NeDP02Boj4RQ83fex6eM7HrzHNjJRDfBqxoKhyHfES4pG5GN+MYT8G1vqL1KC2poheLqq22dB0Ggv3Hub/2Rb0VjjfNnz6nPnpQ/gexrW/UpfzHh5FvKTIhWnEjyC+1tZmKwG+bblZCfFjAMntrflaBvFOxKWGfaDm+0naaqsU4ndOqDPPIt6C+LwxV5kKxl5VnBzmDLYjvq3SspffIQBO1NzyjD+W22+b0pQ/L9eo03zByyPi1+fNQ5Lvk/vMaU8XQp8n1Fwl4C+9JB+sIh5E/AvVi5I/F/eP5VaDGMiQoVcm/blInjpsvYx31ZmSPy/RXdxQfyxZbGgHyoH+E9UuSQG+ZumsRjE2Pmm3pJqv50dlu1D0ffJVatQC/3wbZhMyUDBhDnsFDFgDESQoQhbfFthoaYAGOjIUrRQZil+hZhcb58A88hq0mc3BvsqU/sjIX5P5uM60CuSGPoIWsir8jqwa0+Xwq4VccxM/fiw3Aj8mGSBpsgefvWSS7CPjZAJE8hR5muwneWQnyN71sdXYirx4bqz7eRlnpEw3i7pzaHdBxv5nVJi4pq5ZWRxsDUeWU/YLmnL5jb9iudJYbqJsxTM6da+c87/y1/ll7Ff41UjB/8iv4m/ljrWYd2WZhtnZsObuqgzadMYt4KMxixOLO+bj4smvHoz8LJMzz1/pu3iyYSwORqyXBfpq5VYFTo1EbPqH9B/p5fR76Q/Tv3Fvc59yX3Ofc19wl0DkznLnuG+4C9zH3Jexvdq8htb3nkUexu1ZuuUaq1LICluFh4W8sE14VJiM/AkDwphQEnagZev6vsXni2dPg8PYh/npPpevi1VA4n6sAG2Tf1UNVRocZUqH1ZsBxzdogpFkiIyR8obqHvdqPlSkCql8KgNiamdqIjWWOujhcNbUDrRNYF+4ozqVTVZKXXrMu08gZ1rHba2puuLudPpJMYNXGxXLhjI6Isq6LjKTI9rUoXab1kfBuzf9I/2mxO7DxJbLEec+A7DvTzz7vou4Iy2AJQdg4PGIG8az8oF3Ac48obTsdnBHJBLfAjiNPbv9r74snl8/dTo38RzreQtg7c1O5+/3O521D9D/KsBZ/V+gn3xVg3tulQAAAAlwSFlzAAASdAAAEnQB3mYfeAAASwJJREFUeF7tvQeUZOd5HXgrV3VVdVfnnGemJydMQiYIEIEBJAVSBEUxSKIpaWVxxWNpuT62JFvW4a4kWiIp2ctkSrQIZoAZEECAAAEMgAEwOaee0Dl3V3XltPd+r4uYlbS2z1F3g8TB31NTVe+9euG/X7jf93/vf64yG15vr9nmXnp/vb1G2+sAv8bb6wC/xtvrAL/G2+sAv8bb6wC/xtvrAL/G2+sAv8bb6wC/xtvrAL/G2+sAv8bbazIXfWo4iQvjaejSuhqD2NETxdHLi/i/v3cVI1MJvGFLI37/7T2oDr725fs1BfBcsoD/+K3LePr0HKJBL7w+L1AuIRpwoVB2YW4xb6DPxNOYj6fwe+/cgF+/rRXN1dzuNdp+IQH+wGeO4/JMAZs7qxAN+bCV78euLiIS9CAY8CCeLuGFk+M4c/EK+vvXouj2ocrnQneDD1enMpjn+oXxQaRKfgpBEB+8cwB/dF/X0t5fW+3nHuDvvTiJC5N5HDx+gd+KODKUw9TIIN7+1rvQEAbOjqbRWR/A6ZEkEukyWuv8KBZLuK6vBvOLWTz8xLNYmBpF47p9qGtqRyGXQXxhHmWXBzXRCGbn5pEtedFaG8R3P74df/vkKJ4/H0fY78Hbd9fj3dc3OyfyC9p+LgEemc1gZCaLmrAfd/z7J+H1+uEupjB/+hF03/qbmB98AT3rd2J7fz3Oj6XQVOOj1pZx9PQgAqEquLwhRKjZTbEAdq1txLkzJ/G9b/wdAg19qFt/O+qqI+hs8KM65MXMYgEBrwtTC2kcu7KIf3VHJ9a0hunDk+htCuL8aArvubEZO3ujS2f3i9V+7gC+OJ7Cv/r8WeQLLnTU+3Hywgja29qQLHhw+kd/jrbrP4gStdATCOOOna04N5KiIHgR8Pvx5LMvwpWdhb+6BaG6LmRzOfgDAbTVVWFzTx1y8Uls6olhY089njg6g2yhSDMfpml3Y0tXFFcmU/iz7w9hPpnDPTsbccfmWnzq4SGMzuTwyQ/2Y3d/9dJZ/uK0nzuA//jBUbx0YQ65TAYLWRfSyUX0tddhfXctvvp3X6DP9CPauQPuYA3WddSiucaLp4+NY1N/ExYXZnHmwhW4S1nUdm7FzjU1qI96MT2Xwvj0PKaTJFuuIEbGJ/Frdw/gOZIxP7V3W3cU69tCWNcaMmGZJVlboGZPzGexviOChXQRX392Ar96SxPu2FK3dKa/GO1VA/ib+0fR3xLFdf2vmL53/8VBPPYPjzB8KaNczKOqvhvuqlrUtfbhpoEajCyU8d1P/2vU92xHbN0bkHeFcP2mFpw8MwhvKIY7r2vDgwemsTh2Eg0tHSjl85ihj3WjhPzcZZTSBLS2A25fBFt37kEqlUI4EsEMzfM79zTiuXMLCPncBnKYhK1UAqrDQXQ2BjA3n8FcOo93cbtN1PpflOb5D2xLn1elxdMF7P74ATx9LoXjQ0nUVnmxpiWELz52BX/2wc3IXvop4pcOIDl6DKmp80AuiQK8SPnbsKE9iGR4PS7++DNwlXII1bZhZL6EHZv6cOCZRxGIdeDu7XWYz4cZEuUwNzOBUCiISEM7TXa3mfXU8GGkRo8iFV6Dvo4mHDx8nH47gnzZh+vXRvGW3c2oCgXg91dhYX4ejzzxHL7x/adwcKSM4ekcvntoAQ9Qm/OFMolcFI8dncURxthuF9BQ7Xcu8ueorTrA/+EhmmAy4lh1GG6PD197egi91ORPPHgRLetvxg1v/RD69tyLhv7diEaimBw8hPEjP0RigWY7tgW37OjBTGAtRg59D+X0LALVTZhLlbFhwxZ43WXs6q8hKWrBlt5ahGsasZgpY3qWTHluCB6Pi9obgtsbgLtcQLIURmNdBFdOPo/JRYZO5RgOHz6E+eFT+OwDD+PkpSmUSNj8Pg/WrFuPmclxCkkQi4kEjlzNYCqRh8vlwv6zcXzhiVE8dGACmzuq0Exy9/PSVtVE/58PDOJbT55CbvgFFBanEem5Hn5qXXNzEybGhtHWtQYjp59DMNYCYkHASujt7UNjKIcHvvgZeEK16L3+Pbjv5i4cuTiDR776Nygk59DQtxO1fbvwxu3tSCUXECM7HuhheMPOz+TLuEpGfvjcFE4ceha5mUvwkni5CZy3pgPdm28g+CO4fPIFRNs3Izl1CcOP/Cli/XsQ6drJc+mCi7FyqVyELxCBL9IAhtoI1zYh4s6g7AngjfTL43M51EZ8ZOIJhm1e/MX7B5au+tVtq6bBj9KUff2ZMVx6/K+RHjmM9NQ5Y7u1XVvR31pFMxnFqRd+hFxinMiGkaP/TM5PYfDMUYyngvjYR38LJ+hrx87sx2CmBVt7avHr770X/bvuxPzoWZzb/00cY5h0dbaEQ2dHcDEVw+mrCQQDLkT8Lh6jGtGWtciFu1B0MeyiFvsj9ZibGEZj9zp0dHRi8NiziHWsR7ixB7OnHuX6BniCUZQLWVDeUMomUHQHsb63GVfH5rF1bQu6GIM/cWwG0SoffnpiChMzcUwtunFmOIE3bat3Lv5VbKsG8I+Pz2NwMoNCuB2F+AQxpAlt3YRo2wZqhAu5khsz45e4bpI9meGrYJ3q9XiQSy3g+FgRH77/Lpy+OIrE9BBGUmEcuzRDEOM498zXsHD6UfrrBaCQRqgqyrcEmrv6MEMmvLUniiIZE0NjXJgq8lheugc391+00ZbZ2VmsHViP7jUbceKpb2Lgjb+ObGoRaWp7oKYFbrcH5TIZF42di4LR0tGLy+ePo1TVjEKhhO2MkQ8PziMW9qGKbngmDew/MoibtnRQm19dv7ziABfpAP7qh1fw1aeHMT2fpB90I9y2EdPHvodiJo4iNXViehqpTB79m69H0RPB/PhFLI6cQGLoEGPXURTTC9x2EScnvfjdD7wZB16mBZi5gnQ6ieraRiBYTWxHkJm+yO0SKGTmaVYjlntOlqtt8KG/pQqjs1kbfGiMhXCZlmHi7H7k42PIzQ7iyuBZhJvXIta3D2ce/Rs0bbuXQlZCdmEULo8X5VKeMuf43KI3gszCOLwM1SbIrr3+ANa0VVuqdJKhWDYVRyk+gsdPJvCRe/pNUF+ttiIAP3hgCqlsEa21Adz9hz/FYw9/D6mrL1KL3AxnquHxk9xsfTtmTz6MubM/QSExhmJyBovFAHbs3ImN2/Zgw3W3oKalH5PDFzF57BFkp84SvAUcvJLFR3/zV3FicAKZ2RF4qmrQ1tmPRCpL4SHbnjyLPDvfH21EtGPLEjglmu4CuhuCPLsSXr6YwN49O7F77/VwFxcxO3YZ4zL9+7/G+DuNzr3vwuyF/SjSJLu9PlqDuAFdzKep+V66jxICYQrQyBnEmvtw+eoIzl28jOpYA25kOHdyOI08BXIhSQvhC+P6gZjTMa9CW3aA/+7JEfz2fz2IVCmIH9M3yVz6mjbB08zOZughafbR33po9uq2vI02OID5s0/Q/BVR3X0dktkSRsdnGHO6aA6DuPme+7HupvsxOTmG8YPftnj2yOUk3vHO+zC2UMDs5cOo69pMputHJpdHddcOJMdPIjlyzMImP00sSJI09D1J1ltHM/rOva349lNncPTkBXRuvhU33P42vOmdH8TArjfB7ylh+spJBLtuQCGXwsKFp5FPTFBQAga4LES5yBCtZSMSg/tRyOfgcntRVx1AhmTt+IQXN2xoQNFXi4InjCePjOD27S1oib06pnrZAR7oiOK//PAcSsU86mtqUBerQlPUgyJtdS5fpP8cRoZ+NhCpQ3NdGJG2Tajq2Ekt/SHteQ6pmWGk54aRonYWQk14+kt/gLlSBDtufz/6r38nxi+fxfj+/4bB6SzW770HZX8N5qbHsXHrdiTiSeR5nNZ9HzDzP/bCl+GtiqGqvseERYmNoekMDl5cwFtv6EMw6Mczj32Xy1I4fWkCIzMZlIL12LxlM7b0NRiI+dgGlPIpFFLzRrLK+kyX4QvFkJ44RytyhRHBJObm5tC59Xb83lu76YfduHF9NZoZF69tr8WB83N4y86GpR5a3bbsACfSBbxwBTh35DmcOvgUThw/gcERmt+8C352dkd3HzxeD64efxJZhjTVdQ2obelDVdNaDD39RduHL1KLIjs00roB+flhxM/9BPE8NaC6B3vv+CU0rt2HwRe+i6tHn6DGbqMfHcd0ooC1G3cgR3I1N3QSnbd9FJHmddT6bxpj9obrCDK1fOo8BWiETHsUrS3N2LZ9O8ZGx5Bh2JaaGUJyYQZDF0/h4tAUZudmzee6XF4CPGtcgKYGPsbeuXnGxLwef6QJHr7aB/Zg76YOnkcWeZrw/uYgtnZFUEfh1shWMluwESq/d3WLDFYkDn7zn76MKzN55CdPkfhcQD7JjvL4aS7b2DmtaOzagIG+Dhx67scYOvksart3on7dzZg99xSmDj8Ejy+AQF0PQk396Nr9bhz4m3ci2roetTvux/Ztm3GZArNrcy8unT2KIweepWYtwB8Mo6bvBjLntZgZHcTwoR+ga9/9ZvpnLx5AsK6X4dAGTI1R+vJJxrQh5MteuuQi9uzYYIUBSlNmC2Us5sqYmMtikrF5wOcimQojkySrnh6ki3HBF60nu26jW/AiGAqhIRaGz13G5Ykkxoav4N5bNmFiIYvmGh+2dkdwZYpRAVuWwN+wrhr37Wuy76vRVizR0fWuz5JQ1ViqUAQlRz9GlsIjeugOGU60bsTW7buoD2U8/52/RiGbQsve96OUSyI5dhK5xSn2fQG9e+9Dnr7w8Jc+Ygy3cdd7ceu+bfjGA/8dO2+6Bzs2dODkhWGcOvoy7ZEPPeu2YffaWlydKeLc5THks4u0HLXIZLNoqI0iFAyYuZ0vVmFh7AK2bBxATThgw4MaRxbL5z8ECbbH7UZTTdAGLIrwYC4FTMezZNM5266asa9y3TkKxSitQHZhDGt72tHQuR6nrkwjzGvf2VOFK3QnM/T/1VUerGPM39cUxP/+5s6lnlrZtqKZrI2//EnMLizSDDcjy9hX2iIzqViyzE7KZVJkymvRveVmDJ9+DqMvPYi6TW9CVfMGEhfGntw+uziLzTtvwJkDP8Tgk1+gJnaidde7ce+bb8fffuFzqGldi3UbtqK/LYLzI4uIBFzszJwBoUGGSFXARoyaYiRaLjeOHD0OL03qwqUXsWPfzZhedGE2nqIghuD3e/l7H7UWyFDbGqJ+MuG8Zcfi8QRSi4tG2LZSKFLU6AuDly1GVkydnjyP1Php3P0b/wkHj5zkodzkAy7s3thK7lHG2FzGOEhLXQhN1V7Ukuz96Xt64Vthk73iqcq2d38R+dlLaOjZRhN3hf6X2pBjuMHwQ2w0Oz+KPMOQWP8NCNMMTx7+jo0kBRt6uW0QZYLS1r8N11+3CUfpz0/s/xFS8Sk0b74Te/begLnJIUwQpFs3N+PiRBo+j4ua4rVB/BB93jQ1Z2w+h8VMEck0hSo5hyJj1DWbdmJqIYPF+DyJYDVKLgodQUkkGBrRndy4oZ5WoIDzly6jwBg73LSGYFJkKHRu6nMmzWsoFyhwHbQ4pxEfv4Bb774Ps9Tyq5cvwUuekclkcNsNO3FuNIm5eBxesu2uZkYVPMdUNk8ND+DzH+5DLLJyuetVyUX/0h8/gseeOYh9d9yH4SuD5u9KqWkzx3mSF5lMERhlt+rWv4nhxhAWLu4nA65BoLaTeujj5zq0t7ehq6sTgUAQFy4NI1YTRU9TiPF12UaPNnWEeUVmKPibsjHqS5NpHL04hbamekxOzSA5O4zNW3fg1NmLqK6uxsLwSXRtvglDVy6jtbMXY5PTuHt3N8+xjB8fGia4Q3QpHgSjzSiqq+jTi1n6cBKsUEhJmQskdnHG7nvJylUBEkcpOc2rLiMxeRk3vvEeHDl7FW5l0gJebOxrxuxingJXsGNou5/88U7rp5VoK57JUnvPbWsRZDx6eiiOX7p1HeIF+sFAPcpVTdTUfjLRRgQb+4FQIzVjEe5ICyJduwzUQoHeLxhBuLoO4YCb/UszVxvETZvqMdBWZf4ySe3cSHDrwl4spEvGWDUCNEai9OzhQfrFNkyT9BRyOUtJHj9yiBrmxvTFl7Dx5neR7f8EXWu3GXgbu1RE4MOjRyYRpCCNH/wWavuvR5LMu6m5FT5/EMn4LMK1JIyeMmavHEGUsXddLGpCFp+ilVK8TysQoNkve4KIM4xLpxbxpn0DBDeHoZksGTUFIJVDFUM1DYho6HEl2oprcEmj5lQrt9uFh16cNv8YYOc+d04miwyVn1UZKbOqtGaBhEXDfsodi6W6aKKrQ26SHhc7xY1MrkCZLxNsj71GZrPGfMVQ51N5XBhLUwACGJ/P46nnDuKGvdfh4lgczfTBSkhcvnSRbmEEyekh3HDvh3H80AFyMz+2bd6EdDZnmaiHD8/Sdwdx8JEvINZzHdz+iI1Jb9u4DoePHEWsqQM10TAmJ8eRmBrCTTffglQ6Y0DNKxaHn0Iwg9Ya1XzRnBfdVjESjMSsSoSeg+ebp3B6zMc3x0L47v+xmd+XP6m5agBnSTBmFwu4zJChZId0mWllmIl0ztlGfMNPIPVHQmsgq3nps7ipDapLULK5Iqpo7qTRKq9ZTBdtICORzhN6Fw4NJixM2dFXiydfvoiaWI0x2s3dNfjuPzyD3NghtG+7i3yJWjcziaa2biQW07iRfnw6nkOq4MEZxvFKYjRsugtz1PR1u9+E1MIkJsYn0NI9YNcwdv5lxtpr0NXWYD4/RUvSWh/CofMzSM1PYE17DOeuTpLFV+Pu6wfw+MFhY/LraHku83yLhTxmkkrpBvErNzXhvTcufwXnigKsXZeEjBL0RCxNCaflNEBnSX6yVFkRDuGoDhPYPtpcl4u/4T+Z2TJ/72Pn2W64TwEswcgWFLd6ME8NkAbb8fiajufx3NkFkiY3Hnv+HAreKvgDVehpDKC3pRp//+2HEampR3f/Rlw4ewzhunb+ShbGjXrGs9MLOXKDBOJXD6Fhwxsxe+Yp+BjzNvduxfil4xSKCPzRBguVFgZfQP+et8BNsqgyn/qoD4MTWYyMjZOlv0DO8cs4fOwENm/agCtjczyHIOP1KusLkSyxauXsFTHUk7H/4ONb7DqWs60oRxfA0kbreTYlEhRbyhRFQtRAmlyZaYUxijtraKZlikMKV7heKb9IyMOwhe9BmmS9qLUy53URnwHfHPOjsVrDdDLlHjQwBNm3ttpuS2lqaUVnc0xSbH5VKcNofQfj7+tw/sTLFCaPpR1zccawZNfjkzM8yxJj9kmEWzYwtJs04fSHY5gZvWBhlz9SB68vwLDoon1O0WoIXB17ZLaAsdER2t8EcgsTDMHyWD+wjgya2lp2IV304OzVGR4rjnHS7aoQ/TTdUIqMvIru5uFDOv7ythUGmP9J7V55M21Vk7lV2k5lNHoXiDLLMseMMAx0ASgB8PHlZkd7pOJLAqPfS/ul8QI3xk6OspOjNN111KTWmA8b2kMMjQoo5DMkX9TumTlqby3OD16x21p84TosDh1GcvQknb8zGlVIzVFDyZjJjBWHK6ctQc0xnPNFm0xzUzNXbeDBU9VgAhmjwOVLLly8OoYij7U4fBSBSC1P1YM9a6L05z6U8mlakiCmLp/Auj6RtQCmZxMoFAsWNioZcnI46XTOMrYVA/hayy/zKk2wZQRG5ljLCvxP1lgmVy+BWiEaWn+tgCzh+rOvZWqEbcPPMq/8ZwMatMw2nJelO0hmHdKTJAF6+TTj7WwGkWgN4pNXEVT4RVAL6Xl4AxHzt/nFaQJTDz/DnyJDOMXo2n8xnaCZbrXhyvziLNelkJm5ZOtCfhdDnhKOX5oFV/LEC8gnaQF6b0Zfg8dGkWSSo9FqI2JrulswTy4i17WYiFuKNbc4h4WFBZwcoRQuc1thgB04pHj6LoNt3/lnmklAnWS+i5Jsq+yztFzrrQcFpN6Ept75EtvW/rSJOkpkrKCFbGLTilf1J9+cXFxAmWCVCmm0tjSaIBWSM/yeMTYt/+cJVsNNs1vKZ02zsuxwxerpqQuMd2utGlNJGZ25gC/QhIOM3Mcwjpdgg/4ZkiovfX1q/BRiA3fYgEqS/nWSPn0hVUSWPvfq4Hn0r12Pc4yVcypMoKDY8cnRy/T750adnPVythUE2AH2nzZHW/VXollzPpUtqS+QpN36U36X2HInBFIiITC5rkAwBaq+50i0tC/+M43Wb7Rcplumvor+WmGWQPG58piaT9GMey2uLqQWDOhATTvcBMblpqYRdDf9cobAuxnL5hbG7IyVRy/RjNq4MA+Qmr1qI1XS/lTRi5GhK4zzHfOtOi4JgJIhWYZ0p4aSyGT4W5p0ndN0qoz4YsZy7wG/j8ckuEqeJKctZTrB8G4524oAfK15rjRHU2VG9YWaRjDMVAssLqr8RqVP8rdi2zo5l1AmyAYkX6pHVn1UKutorUKkPIHO0wwqc6Xd6N1P39wac8pj1fFaXqbWpDI5q8qQdipdqqI6f7gBLi+B9wYsvVhYnOI6P8/XY7e7CEiVA5XlL/Mp+moSPAqGi/seOX8EpdQUtT+D9MR5y8aladpDIYZCI5M4NUK/KgDpy5u712NobBb1YYZ6iVkjgnNxmnXqcJkCkE4lcPRK3PphudoKAizYnCZgrXG5fKWAEmGSifWTXCnhLj9qiY8l7auYc5lnaaYscG4JQOJJcMl2ubzABfK3Wq7jCFxpsBi3bmvZsKYD8XgcyflJduok5hNpAuRHdmEENa0D7AGfDYD4quoN1MzMFRsIkRC5/UFq+oxpd47+WdZAaVU3fXaeGpelJhcoNL5wPbfxmhnXb1WWlKGvzsyP81rcSI6fszsvEotJeIpp4wQeFE3A84vcv8gdhUjFBMqbL2dbIYCvAVVNePM/gaR1AtdHk6w0o8AwoOXM+E9xbsW/qsm/VoRCAkALaiY5sBRi6ffaVBqul4RD1Y21YQ8ao168Y18r3v/ut8EV7WDoM2B+Nj0zyGN54K9u5nnSxxOUUpaaRADzNNsu+mMRLhUhCBj5a3+0hYANU6MJGn1ykr5WjNlLHx2iedZomT9aZ4TJS59eSmn0jOaXTNxXVWOlPhmGTirei89O08IUaapJqsoOoLIAuo5MbomMLFNbEYD/cVvC15qkNkhwdGCN9ggeAa0msGR2tUwM2/HVjq8V8RLwWu+Yb2o6hcIA5u8lEAJcAqT4OhKkyeXyIn/bGvNg20AnNV0mVr500m5e87vzRoz8Ph9yBFZalKd5Vi65RJOaZjikQndpZi4x5gyK8JWZHbLBEdkZCYnqsvLxUfOrCqUikWpqdsqutZBZ5O/z/N2s+XT5XNofnidDRGpxPj7Fz3QvPK6rkMKlyaz1xXK1ZQf4n/O/QknaK21RGKR3gavPYtLylwJIL5EtvQssWQHFwEEiKjBlxiUcMnv6bssJcGhpmcDW53myVvlkJU7UJpeqK5QDL+Yy5g+ldYvzM8aeC9ReaZh8pVitwqfM/BCJ1aJT+E4t1B0UMsXS/LSqVBIUhKoYzTrNK8MnmW23r4rn6EGe4KnujGppwqSSX5E2Y+b8XqBZziTnkUyRRfP3MusSPB/j5DNDZPDL2FYIYCJzTZMk2yLDXh8q6+mLCaAWW9KDn42Ecb2EQKZZm+qzhMDCKtNdLrR9USAYjgQItJIdxse4LEOtHZ3NYZz+rJHAanB9LplHNFxFjawngHmGSGMEkBpHP5tamDYznJkbosmtt2I6ESrtTe8iXxICDV9K+vI0xy4SrQDNdp4xsosCIK33M1b2MK5VjllFeWLShTQB42/smnhBBfpmabQ7EDKhEpGTedYyH4Xt6ujyZrNWAGABsvSFzVFo/mcxj7NOr4ogaKni1hw1TuTJMbfa3jHVtr2h6dAugU5FtQyYHYfb6k2jUtUhn2m0skvy5RfGM3bnXw1DI5XKJBJz3LcLkbaNSJMgyTQW4UVy+BA7Oc1OVh2zU2Lkp6m1hMbsFfOzBZIqab9A1EnKCuRproPVTXAbmGma+6jVQ1vFCv1vUX5dZ8ftRdgUQomsSZjK+TyPl7MYWzG6rIQcUt6R8GVryw7wP98ElSFtwDojTPYFefpZ4akERYoEQz5WJTAm9OqcnwkG92BCIYtQMlC1T3s3kPniF7FnzbCjOqrWWmWRgCvTaRvg6GltwNzwKdT032ilruMnHodPmSTGt9K2quYBghmzFGaRZMztCzlJEflQhjEGKEFSPVm4fbtZgAz9qr/M0IpaWFXTaFpZlkbS3EswBKax73Tchh1lsoN13RSWRYQa+ggs+4LWQH0hTVYd2nK2ZQV4qf//SVPHCwLn3THZ0tYKY1aoIxGQj5Qf1Y5sVwRSCFbA1bvWeUil9bHSuMiaBEUMXQMS8r8aAAgz1gzS36tkJxjwIdbci/joSTTvut80TFUlzsHcxrK1Ww8JmLRLIVJBuWj6ZG3jD0Ut/g03rzchCNa2Uxh9cOfjJFfNdBP8rT9MmRT5auPvyLIZMklQ5GdlGRSDS6NlmuXvtWOVCNkx+Rco/hzHwRUNu7Y5i/Sf81IHUmktOaHCNuezM9LkJ7gC/WeNYOnbtfs1sIm7MlfSDDVbq225sV6Kq0W2mmoCNgihk0jTOhSpzrGGVnZ4mD4wiurevUiMnoEnVMNlfhToO0sEVscQKB4BQ01WKlM7lnbpFtZg41obQow1tZEdzyG5mECspQ9tMS9DJGXJ4gQ6RKI2ShB9ZoY1iGE+n4KgW1ctxqY5L5F5a7QrEGslk48v+81qKwDwNQCpGUJ6lxY6ptgSF0RWSQl9p0U2ViwmLKIlP/nKywFNL9sZgZUffmU5v3DHtlrN1rtMgwVuLV8y2xqMt1ErTwGhxn52+CxNaJ2Z3e6bfo1+tB5trS0kU2m6kIJpl5vAK9YVk9a2RYLfUF9rWsmzxrrORrtbIsR4eUdvlAJK4aXJ9Xj9TsKD2ixgnWSGz/alQQ6dbKRtsxEwpUkL2QUKRASZVAK3blve+bqWFWA1B4hrWgVzvmfyRdMk3fWn6g4xW5WU2lgvzaqGCcm1EFgaD9aQXsklgHxW1lPlpxnnqwJupb1iwvWFL37WMKJKfZSu7G8OIeIv4uCJQfPvFvvGOukvFxCs77OcdZkka2ExTSJUb2GMmgrrSsUMw5ew+VNPpNXGnj0Ee+78MwgE/KhtUhFgwNKOylGoajSnWFn5bFoFETllqhSOadRK56nhQTHzHEOtMq1GIUM3wfOem1vAe+/aasderrZsAEtypXH/BGDrcPV5Gclc2YbWNGtNghpVLLrsDohnTs3jLx8ex0f/bhDv+9Rh3PMnL6D7A9/Czt/5AfZ85Mv8/jz+6JuX8dWXMjg2yp3RB2uAXIJRYcxqFVmqnIRNqFLlQUuNH2/f14GBrhgy2SIaIj6EY80GMnsY4xOTqInFSMBOU8JyXN5O00nS1bSOvrTVGHGocY1ptyZlKWfpl6l1CpFiET+mpyYxx9hb2TZNsCaGLWas7lUKU9rpVQxN06O0qFeh2cwVhlK0BNwutzCOsrcKvZ1NaK1jvLyMbVlKdkx7+Jejj9PeFJdqmT7rJVKlbJRqlFVDNZsokFgBD70whXOXRzBpw6hcfu5pFJOT9FUJFJQZEvNUbbQnAFe4CXl2sKduDeo6N1ll5a17NuOWHd24Y2PEtNhJV1ZkyrksuQAN143OZflewunRJM7yNZssY/DyEMOWSca/o2jrGcDo4CkLofTryUMPoW3f+zB9+gnUdaxHMdhMRj2Fd7z5djx3PonzT3wWG3behDUbr8MTL5zGjk29Ng49PD6FhcEXbR8iC8H6boLbaIRNeW4lPKId2zB/4Vnz11ZAMH4KnvZ9+Nh9W/D7773Bznu52rKUzUphRudyePz4HL7x3CR9K7CWcWelCWR1+kKa2sLtTo2k8MBz83j025/Dwvhl5KYHkRo5SDTmLBat6tqLQOtWhNp3INK9i+HLeoYndfD5aB5T4zYd0szEKCbLjbg8kcKp8QJ6GhhnKtu1pL3SaoVgTurT+a5VylH7qEkidJPxDNW8AYnhY2gf2I3RswfIlKvhowa3briFnc9YlfsILt1pEeC63taYTc/gp9keHzyGvXv3GaHS0KAGQJTZKvmiPBZNNk2wTyNVPK4lM8iqbQIYfwCZKboLgp2du2rLxkmeH/rEL1sd2nK2ZSu6O3o5gX/3jctWiyytuW9PA/7je/rMbClNqQKzkdkcLkyk8cPDcXzrW19HeeSAma4Aww0l9X213dixazfWNHitulJkLEWhEFCL2TKm4nlks86ITXWV24iT7hRoJ3vtqA9ioL3K5q3Ub73sVV2YUp1pWg9Ve4i1qyhPpbaXpjMYms7yfIo4c+w5ZClk/VtvxJVzRxnnbkZzWxeC3iJOH/gxqrt3wk9tQzGNvZs68dK5Gezb0o2vfOaPcNf7/wAhTx4ziRLG5p25PHzuIqaunkFeCRNqcJmWRwRD01PIf2s2ArF1mX+VDAWaN6KmOoJjX/6I9eVytmUrfG+J+XDrxlrr6Cj93pnRtJWr9DYx1GBPSzAn2LkiV+cncrg6Poeq+nZLzkvKFUowEMWtmxpw00AEER+JkqsELzsr4CmhJujCQIsfO3vC2NFXbSZ6Y2fU5qlspo+VP5amCNQAfbTb7Yw+0WJb1YVWKqcgJq3zEeAy58lMHjVNXRgbG0Njaw9Gzr2Imra1GD33Evo2UquHLlH7UqhjSNRQW4OJ2SSJlh+t1R5s2fdGTM8t8vhBI44Ts3HU10aRzuaRotkv5ZM2SiUwfZpmgoxa90MvjhyzeFjpSZ1MIdSMP/ngXmxd26quXNa2jHc2uMhavTb345rmKpIRPx45Modt3RELV2Q6pcG6O//qVApx1DmdTtPl3DlP4pJbxHSpDoePnMB0in7c40emILOr6kqGOERKo0xCUVoqM0vCbT7fvnO9R0kEAisWrdBLiQ+FXgrBFF4JXMdoq+CAbiNVwEZq/ky5CeeOPItdt7wFiflp8oGfIh9ug9/vh89McB471jXZIElfS5hWwYX9Ry/j0tgC0qUALk8msTA57NR4E9BiZh7J4aM2BZNMuBIbCp80GqVSIX9NO1Jjp21Icirlx5c+fjuvwed05TK2ZQ2TbHyT7eGDE/jct5/F0y8cxO/+PyIcIloapC9jnh06u1gkI01aN1O86YN8yPGiS5k5HP3a72NwpownnjuKrzz8Mr7z9AX84JmzNhXC1Xma7IKbIC8NH/JPJbahgAOohMxcAleKcOmzabWlO8s2MBEi+1b4pDmtmhjytNURQGr7ndvq0dC7HQtJErlQHep6dmD61FOIRKsxN34VPd1dFJYyLYef5riIrz/8Ak4+/wju2x3Dpz/Ujx//4Q587mO34Ob1UVqqAns2ZCNEoDsxoqgBfrJxDUtKyiS0Yudu3Txex1CwannZc6Utmw9+4OlhDJBYffnR0/jWE8ewYfMOu6tfc0r9/W/20GwVzQd+9sejeOToLBYSabq0ORuGU8A/f/YnDDvmSbaOW9woAVBsqqxR3ZZ70bj+VmST85RyD26/eQ+PFSScZYthVVPtpCd9zmfGpLImCqV0ddJcbmqfBbiK5nW/rt15SNJ3YiiJgbYQHj+RwLPHh9HeXIcgY9sTz30fsd69qA8WsXFtFy0Gj0dXMDqdRkOsCr9zTzd66ys6om40kaV2A5s+8l3MH/wKqhp76MN3k0XP2zbJibMWOlm+O5dEsPsW3NRbpgbfZb9d7rZsGlwXoub+ZD8+/1/+CjNPfgJPffrdePnP9mFP5LKtl9YJDM1Lmc7KM7I73D6SLJIrEg8xZc1+p8yPwgp1hm9pWuCplx/A2Uf+MzD0JG6/7TYcPXkOn/7OUYY8GSNezv1MTnpSca+K4gWu+lwjU0oFqu8FrpqEQEOIEgL58L7moIVSu3pD6O9owCT9anvMjXvf/m67xfOG7X2IkdRpBEvH6m+N4H+7swXV/gIO0+U8+NIcHno5gSNXGH7RF/vo+U988R1YCPTSBWXtfmRZG92QphJcJT1kgpTVSoyesNh6pdqyAazZcfr61uL6fbtQSKRwz29/Cp7mnfjd973B7v6zHmY/97IzNWhPlkHmHKV2+WnNqhmKDDAk2s3tSujcfheizWvRceOHyDpryEaT+OTXnkCx5Qb81a/0IFhawHvftI3mNEf/CiN1tVGn8L2JhEsASrttlEmoOnaa+668BLLLslK6S7+tNmAFAppMdKA1xHPyYHgmA+UctvXWYl1LwIrbq+h/JaQ3ra+2a/rys3O4948fw7v+/YN41+98Ar/2qRfx8OEFTCZyoDzgv33it5Gv20IyRXZNcDWVhc3hpbNgjKwzmbt4AJv7V25W+WUD+F03daG91off+tW3o2rdPmypK2Casa38rmV42MEaQWqI+tBZFyDpYTzq88MfrbcM0bptN5JROyxyanQQ7Wu2YvSlb2P2xA/Jolx46IfPIbU4j92/8WkUbG6NAu6/vhE3D8SwpTNiUwoqYyUcdSwbMDDbrPh3SXXZLK3Jd/IuA6tZIOuWl6gfcfIDzY7T3Ri20SelubsbAyRx3I4RgTRdjF3Xc/BKHn/4yS/hQ3cTwEc/jD/52PtwfP+P8Offu4gfH4/bPVMfuqMPHZ09SE5e4kkVkV+ctCFCESz5DTHrLO35/betc05uBdqy3h/c21aHzX2N+IN//T48emgK77i+13LPymSpY9kvxoKvMAY9P5axCg2V3WSLCnHKaF2zB113fAzJvNum8tXgeuOu96Fx329gPuNG55qNeNutO3D77jXY0RVEO0OyBprYCM2xKj5oi3kWDpgC15SWFuFammHg890BneyaP1PqVPHy+ELOeEJPUxAvXohjEwWnnxGBsnD9DPdEEBUNhAI+/PW3nkfe34DrNnTYNMdv3tWG+u6t+OrXvoGJcoeVCMkdieE/8swRhKrCFr9r8lX5X08gYkQw4Mrg3374bju3lWjLRrL+uaY75xw2S9WlOkib9V3PMPrc46MkOQX6S689DmchmTXf1k5zmSDAM0lVSHqQSGbQFvMw9ApYOKO7BGuocfU0p9IkVXCoOVexZJL5XrkqWyutvfYy+d224iIeAlemspYASRDon5yasxvJda/v82dn8bv3dNldgwqPBifSSCkM4u++8sw4mmIhno8Pb9gYI3uusXHne//wEbx8cQFvecMu/Nu3t6OlPoTGN/+luQH1geJ+sWcG4licn8KejS148BPvcc5rBdqymehrmzpTmSuBqU6U1FfCF2W5epoC2LOm2hIhmnmmjaHHlm5+p/lrjvmwqzuAO9br5cev7KvBR+9qwVt21LITq81MNvF3Akjap2M52L0CruTpZ83AXQJ6qVXA5SlZIX0tz0GjVGLXG9vD9qAPFR588A3tJHGqgHR+J0skkPV9z9pq3LqBrqEhYGxcD/fQxKR/+dG77D7ikyOLdi+06v46O9rgCtTYgVU8oMEHBsVmGe66ZeWmb1BbEYAFpvytOluvpf4xDVaLkdjcurEGW7vDlhVqp//UPJLyheo8vaQ1m9pD2NwVMaarjhQ50+2n2v+1+3WOs3Q0KwR4BU6BbftcgtjA1fKlH0sIxY4jAYZmPD9Z+gGCrEpMnW4fLYdqxpSJEzvXnFfS+rH5Is89wBg2YLPWT9G8S0A6a124/87tmJ1mKJgu0RcXsPe6bfbsJhEtFcprNIlBMuKZAt66r885kRVqyw6wNKqiuTJJ6lCBrToqabL8rorWVQT3zl0NeOuOOtxC87alq8putdxGQOW7bNyVHS+Co32ZUeV7xZ87UPGlt6XPlY9L2Nl2Kp9RnkM/0X60zt75n17iBMmMKkvKRqTkg+M01fK/YsqaULRye6sAljvJFlx47vBZsuhpCpDbhNHy5jTf3Ay37V6P2ckRuxMjzf2pDEf3QukMRfGyDAcXJy/SzVSZ+1nJtuwAV7TXNESdqEtSRzJSEqlVCKNelqa0kHVr1EmkZjM7dAM1p4cmWHntoOwvf2v7Y0fZzDT06RqxkQCJUAm0IjtWO9Sf1umYAsu5Ga1kpUEF+ju5BoVCznweSro4tdMCUSDb/UwUPoG5pTOMqySC9eQHypLJ9Cqv3Vnnt0zW0Bx/P3YEzz/yNfz5D4ax/3zSCgY1D+ZkPIf2BhIoHl9xtny57gs2v6uB/2AU82eftNtYPv17d6rLVrQtK8BWhipwHXTtXR/Z38KUnSe2bHrG5U5eWetD7FRptkBXvlm3nsTYuepw3coh9irtmCHImlxF4YxuPtMDPjIEzLQupWoRFRQUjSxNxbOmiVqe0KPs+But128zfNc2Wq/96DZPnZYESaNREgwNkjjXonN1UqyaWUATjJ4ZSaGruRbJ80/gzCN/ic/+4BS+cWCOJreI4eksUpks6uob7MeaEW9sbJzXRhOtXslnqc2zKAfqsKZz5ac0XEaAHXKjoT27EjZ1ml5OrfLSaI80jNsow2SDADwDpf9kvgWoFbBzG03cqTjVzd8qC2ag0pQq7NKIlJ6MNrWQt/tvx/kuADRcpxGrGb6yVBiZUwOZgiAgJSBZoqbZeCQY2pc0TwMEae5bsbEqOxvoHuQioja7D0+Qwqjsm8Ko9no/zg4vYN2uO1FyUyNRQH7iBB5/+RIePDCDFE2VrEZ7azMFQwLvwsEzIwhEVTTvszsgor3XW5JnWy+XrXBbJoDpd2mCZYZNe6WuBEzaSiNrQFbAlSaKtMg38c18m9ZrxElhRuWmsoqZdRi4qjU0eYsD7jSBm+b7BM2hxnZFcCbmc6atCwq5SF4kEIsEVhqtW0xFgKSZcfpU+Uudy8/uSuRfkWBIQGVVNEqlW2gEtky3LISEI8H9Z3n+/SReitVb99yPcOsWe3iH4mhptqpVVNigZysp83VsKI35mXE0tvYS3yorlE+X/Ph3793hdN0Kt2UBWKDKL0rTpAVGbn4GsqOR0liti1Jb1ZEqtrOONmlwtJqrbTlxsEoK+WyVwQgs7TtBLRNYs0sxqxiqNFSg636kKQI+TwA1kDCpbQiuwMlIQ/lSHlnnqWPIGghgnYu0WoIk66NjGqniq1IFIrLEVfb0Fo1zd5A5NzHSUc2Wt2kLGhrqUFtbizD9tfatc9veE4GPwvvVR49yXR18Ab9VcOhuiWK0F3ddv3LZq2vbvzjRoZ8LHJEX/TlZYPktrXO2EYCmJ6bFzl0JAkAFeCpr1QJHMBT/ay+ay9HRNGmtM9/GK9/1rltdBI6aQNFgg7RQQ4I6AwmZiu6CNPM6P52LMwDBz0vrBLaOqyY3Yjelc5lchYHP5RKwipCoGkWmXr9T2HZ+PIPjVxeNXes69jK213MQdT6aWT6edeEjn/gObrvlelwcnrH9qYdmZ+cw9LdvseOudPsXA6wslfaQYifIb0p61fkhmjgRDDOx6lRuYx2tP5peddLBSwnLTSvVqCbzLNAEkDp1kZ2q30hTBbDIkXym9mWsmICImElAVOwuDRVRku92GLFuSnOOL+kRkfOz9/UbCYCSGbp4rVbT7xyrohvJHQYu16BjnRxO2bkITE2b1EFG3V4XtH2pHo2H5zmXLbnR2xiwosLPPjaCE4NT2L2pG888u9+K4+Gtwr95oxfvu3P558T659qyaLD81JMn5jBKPyjg1EMagFcY1Bh15rFSxkrpPq2noiFJoA4TYAlAPdc7RelOCawjENovDFyBLRYsf2o3qfF4AleAClxdgCo3KsKjTyp2F0FSLK3Odxh82Qb8+WbrpGnagcy/nTM3FGBZCpLMtpbp2hRKicRt7tLznHS0ss1WJ3ejJm3XMOXnnxizUE/+++RQCv/XQxexd6Ae45PTGB6fxZp1Azh69ARGHniX/W412rLkolOkrH//zASeOxt3fKP5TPrbcIihHzU05Edfgxu99S5saq+ycMOK8wiQtEO531qGRdIoldZIe1Q0JyKmDtY+FQPL/wp8Aa5OFdh6V5ZJAEs4BL7eJTACUIP/Wid2XtHakNg5gXU02bkGCZq2N1/M8xIh022o0mj56y3dVZbtOjeWciY+jXjsGrW9hOXhI6rUALXab+7nr340zN8WMdAaxqMvnEV7S6M94e3gU9/HpYc+5hx0FdqyDTZ88/kJ/NdHR2lCy8aM5xfimDryPSyOHrW72mvX3oJNb7gf/Q1eq55QhYemApbvUsGcOl6/ExAVoPLULLHzBFmxE686uWyZaQGrBISUWFegkEShmCwEd0VzL+0qW8JEKU69i/RIGILUZpsTk+AIX5leDQ3qmwAXqNqPBEnHlcDp1hQ9z1j3HZswcDu5AUUFL12Ic13AMmEauPgJrZkzJyeFP53DIJepNk1PXAsnL+ILH1+50aN/3JYNYDU9kGP/2QX8/dMTeOn8LEpzF1DQrZkM7BOjp9Cw8U3Ydcs9uDAax51ba/G26+qtowSmzKOIjgCWZqqzBabWGah60VQKWGm1kS8CwQsA+9hMuoiTAJKZFoDyqdJeLdN+w0Evj6FYV3E3tZ7H08C/M6GLY7YVJv2M4NFHyFwLbMXqIlCKieUKNNOtgL86ncPmzipzCZOMx585PW8TqciCaEzYZpvlCW7sDOHgxUV8/N523L519Z7AsqwAX9u+QpP9mUfHsZhMIbc4a2Up2zf2YHRqEXdsqcVbCa4RI3aSTLA6VqGJQFbnK2ThKguXpEnaxuJnapS+C2ATAmoxNzdzKfDkThVXyxoIPH1WZ1eEx7T5Gu3VLD8yrZQZfl/y7Tym/L1y1IvKmHCN4lzTcv5O2Ta7oY2C0FQtjuHH+EIW58fSNmXybZtiBq7OXyGc5s+U0D10YBLHPrlH3bNqbcUAVvv846NW0KYa4JqQG2ubA7hlYw1aYgGLS9WhMnFiq4pn1SHyi9JQ+UAJQJodLTMsLdbLyS87IYzjizVQ4JhZmU6hJnMvIER8lLTQFSpEEvmRqZavNaAIroRJv9FvJRzO/hygVe81Mq9nPziTnqqwT8fR/kUARRzlYkQAR8ikP0W/e+vGGNa2hIwzSABkReS7lUu/TG3/1Af7rW9Wq60YwOqkrz07gTfvqLcEhGql1MnqQOeQDhjqfQFa0VKZZOEkjZJJ1qbSKGmstjMN0/YEWntR3Kz9pLMy8w7ZkmDoGMqIVfy7yJaqK/jRtFYAqwxWpE77l6Zr31JnI1Y8nooADl7SBKFlKzbQcKa2F8i6aV2FBzLtSpd+9+VpGxf+wM3Ndp26BhM67ldu5cDFBfzePd02/LiajbK8Mk2aJjNn9ccEVx0o82rY/gxc6aHCGJlPVUM6hXPaVtrtaJq2d8IuM6v8LkDU0UGiJT8qDVOts4AUuFQY86d62Qw8NKnKqHG3Bm7FDeg4Oh8dX5ZDflvLtA9pfqHowg8ODOH7LwzjH47FjSzpJjZFCXM0vWdH07RQi/jhoRnIFOtBWL3NzlCn4mUNIypiUDilcHG1wVVbMYA1GCAQ1JyOdDRNTeA5SGu9o7HqcHW8/KWAEeCa60oaoJe2EQNW7ZXmkOZmNhCh2NkpBFB26hWTrO8Cn7Jgple/F5D8Z65AwqDicw3e2zqdmOSNTdtoHxKwHFU0NTeCA8cu4GvPjOLBF2fwg0NzeHkwYa/Hj8/bk8A10cveNRETUpltuQedp252q4t67emnr0ZbIYAdsysfpSbypA5XxwlcpQpFOgSxlkgzBYSEQJ2qjJMsr3pZv6HBtW30cmJVByy95EdNA7nOfCy/S2sqLJqLTYtF1sR6aVjssx2ZJlrg60ToUu24Fj9r/xQkmelAuBqhWCutgBdz0xO4eHkMh89P4Ken5qm9KbMmtVU+XNcfsaec6Vg6rgidSm3lf20IdKkvVrutCMDSEE1ff+uGGIam0xicTFtGSixX/klgq0lLBGpFWzS0ppBEIzYCRkyacmAASjN1sup4mWQBqzFlkSNpY5USGnyXRkpodAh+dQSE22h/EhAROkezFexoSy13fmOhUcXX8yJEkgSO5sDS8x301FSv32cP0VKYpBEwpWfb63x44+baJSGWmZdrkO/Xu2PyZa5fjbYiJKtCLh54ZtxyuBMkIeo81S5plOWNm2P2bGF1oMWc7FD9xn7LN2OxXOYAohBKA/+Oqol965QFirSvwry1DwEtM6/lCoW0TBqtK9Q6ASB/rM9KUtifocsN+K9yHjoTnZfuY9b9zhoU0VZZnouBx30qb659aFDhg7c2o68pZCGbBNXchK6F5yGLoESNRrs0GLHabUUAVmwrTfuL7w9h/7kFapsfOXac5o0UAdJrPVnpzQOavcbJNwsYaYU0XC9pWuXEdIoCXVktabhSmA7bdtivlms/DmAy60tgcp9aarevcJlpPX+vfLTFtVqtxgMpxpawCF5lrzREqTHg//70pMW02p/OR2GWwNU5KcX69l0N2Luu2kadBKbOU7uVlZDJl2Dqd5PzBexeE9XRVrWtCMCawv6zj4/ZU8f+4aUh6CEXuj1Uj5cNhqP2WHaNjcbIfNc1+6zWWQ+L6m3SnQM+q7pULZT8qsInBzpnhEcdLYZOS0rT7aQVtUzdL1MorVG4pKsyc859SFhUFisABLTWibBpvcI28/kUGAmPOkPWQyRRn7/01Lg9IUbbKvupdKdKjCQsb9tVjxsppNpevkS3sqroQcfSd4cvOMcfW8hj1wo9/Op/1FbkyWcyz8eupvCJB17Cht4GXDp3EumpQR7Nh8WZUQyffAbjZ59HIpnGbLEaC3kfkllpp5PEkK9WRwkIdbJAlLaxnxwtE0HjSnWeNE/MWusFXkV7KkKhpvNxNJ7mm3t0tiWQSlLxuwCSkChelYA4aVACz/1OzmetJIirUU1zrPXyp/cS3N00uRIMHUfCpXftR4LiodDIVEtIhb8sgkpsV7utWKLjD78+iH/ztm7zP/vPk2Rlyrg6kTTzG0+X7Glko5NzGLl8DotzE+zmMjZu34f33T5AH8fOZIghHycknHyxM4hRAVsdrncB7uivkx+WCRaQ+q0z0uQIg23MJq3SoIRMqcibBEQJFQvFuF5gKzMl0xrmOagO+pHDs2ZJlDRRue+d22qtqkP+3bgDf2gmnPty3IMO57gIvXTOyllf1xexc1jNtmIAH76cwI4emSSVvBaspFQXXQllFDrFSV7OjWdxYjiDocmEmWcNxSnzpVyvESP+RsN7GjlyCgiczlSnsh+XOtLRai1WhwpuaY2T1VLH2+YGpj5xsW0j6iQNlPCoaZBDTbXQAjTGGFb718iQ3II0sFKgL6KlY+mXEhQJiIRCIZKESGRMK5W/1gkMz+awpes1ArB2qY6RdEui07SFutlMVY7qXAEsMBVKKIWoAXUxVT1U8sp01kZlxJY1eiO4pBHOHfz0bxQQAV8RFmmcOtgB1ulYAS2iZ1rFZWYytZBfBKFlwvhdca5AUaJELkFare86vqyF3WnB7WVBtH9psNyBYmzdX6xl+o0ESddlgPO7UqJqpsm2B5eV+2zrjjrnsYptxTRYREtSrXTlaYYbZ0aSlqvVYLgAFVOVr1ScqeJ3zQyrQnERGg08yA86pIgnyZeZX56pOlHao46SAFh4xM9aLuAqIYpEQ/7WfCR3oPX6rNhagicmrg01xixzLiKk30sIlYpsiPhN+JS4sDQpwdJx7U7GJfAlBA6IBJP7F8PWN1kKCZDAlzBKyFUBqrkzK1Ugq9UctViBZsDwXZWNSsY7WuSwT61Rh2u9/JPCEM2fNUwz7tQyqwbrlWI3ES/tR6ZUCiDmq9BH22gfAkb7t88Ckdur5MYsLheKfc8kCkgwtpXAKSbVwMYsj6tRH+WVZ3hcTRIjYXQqPp2sl4r5xMor0zKpvIeHsutT01WYADhfzDybMJUdC1Jp4h4679VuK6bBatJiXaSAWiQYerKnwiEV0Omw0/EC5lLOfJXSFGmajf8SJPOL/CftshJWdpq2kcZoPmi9SzvUmboCfVfHq2m5Pjvhi0aGCBjPhTJhGq+OlgXR7SsCUd9VrCALI21b1xpyslQM2DWIIaGUNdJ+5V4EljhClP5WgiWOIJDtuPxuqVgKgp6N6AReEgjHRzfWLO9ssv+ztuIA68Ikx3wzCRfAdi+uNIkapFrnOXashNvjVppSFRqO1urMpBHy1VXsWJlJMWAJjVKAehewFjaxY6UfWlZJNghkCZbKg5zCdwdcB+AikuxwabSOozk6BHJ/SxDdjUEbPJDv1aSoGhbUdchES8giGkjgOVVAF5HT4IhTF+5ouo5/7bmoSeD0lNHVbCsKsPasslp1jpqkfWQuiwvjafOpVgTPbaxgnX5PxErgqyP0W/ky/VKdqO3VVDoj/6a4U0sqResiVXq3dCHftS8dT6ZWwmSmmWZY72b6aTUEspTctkkk0dpQjfXtIfOzGuaUn3U0lceUJSHgdksNl2m0iLBb3bXOUVWbzh2UTpZLfzxNCoFJoPWBzklDp6vZVhRgtYoW6yAyu/YEcGqhwgkRnRwv2m4LYccLaLFpAZBfAkFaIlttt7WwkwWuNENN7zK90nYJg4aXBaDIVaVIT/5c1ZgCVz44QUHSIIWa5q3K5zI2xZEmPN2xoYdgwsBTMkNgSlvDPF8RK1kNMWmZaGmzwNc56LuEydmnEjVOyKaL1rWbFvO7mHqM+yXkq9ZWHGCnMN4JI6Rl8k+65VMVk0pE6M/8IYGWb1aZjCRd2xol4dlJCdSkK9JuS0nytDXqpN7Su5i5ivAkHNJMjdEKYOdeIVoFaqmTAStQ6vg9pYlP+blMP5zPoqV3I3obq2gpXGaepaWyMAJU7F7aK8GUT65RiMTjVgYy7G4GXpeFXwTXZtuTw680feT1C2ytl8tZrba6APOzbiTT/bkiO2qSfGm2QBXI0khpnArj1TEKa+TFtZ2UQtvqlwJSFsAIEsGNU0O1XDehiSVr2uJkKksLoecCs0N5mZoEJZ8msIWszR9ZIrBa7o11YvP6fgTcJTPN8rnSWA3ci2CpFktg6o4GabU01yneo5vgSQWVe+bZWZRkZ6k3R4sr3as3Z9afMvSAzNVqKw6w7kUSqM5np5ZK5lKERx1QISLE3jRYplasWpqqEEa/lNkzgsTfEn8jZzLfSinKf2oeDauhoraquE0z0hZyaXswpR5xo2cl6CknmhNTM6zrgRuaTkGPsoEvjNb+beiq9xtjlxkWexaLlmYqFpf/F7nSsnoKgBIdAbI7nVulBEi5DQEo0qdWIVu6MD0vSWJJPTcBVjnParVV88EyX7pkASUmrXd9d5IFjskV4NJMrZOZlc8SiZIJFuDmT6mtdjfhEgPXZ8XZKs81o0Cz61Iv811zRep5R3osDv8zYTNOz/PRAzb0HIVo6wD6OxtBpbSigWp+cLTW0VJVZRhL5jJN/iKTXTHNejeTzN8a8RPoMlUCUwBzI02Mqo1tOV9ZCqKYtG22Cm1VTDQPQxNctItW31+ezuDKZNY0QWk+ZYw0HKdToRxYxqlifjWHlUy2/OjkfN6+KzExmyxifD5Dbc3AVcxSexTKMC4taPZazXNJM5yjxkqM6Gfdbi+1WBNy+6jBBJzb6kEcves22eNepb2yJoqxNXOezLLKbeSLdSOdyoBEvlSKo3NVCCVfqt5TLlz+2Qghv+tNHyquqQKmhEvXqJEmhVWr0VbBRKuLgTFqWYakR3fl6S78k0NJM7F6Ylkl9SgzJsAVMkmTdTO3Rp7ErkWaphPytVlqMU1wJmUdatWSSiiILKXj1Fg9Os55PKw9l0hguhma6CR4LtrOutoXQqxtPQbaq7kP55ZRY8cEUORKplcaKk0WsAJcplUmWy8BrOZ8pnBqHzwPZdjUHFAdQNVsYjZ94H9yK6tVwrPiAKvpELoTX0NvmzrClrDQZGinRtJkvzSlVHJpjcIcMV8xaN3+oXFYxcWL3CaXI0HKptg/zjCgJjkJVwVRHQmRnJG0zY/YRKaaF7JEc6z+1WxytMPQ08cErk3CzauVadZEp/1dbRQumVeniF3m1u5+IGCq6JRmOo/nURksQabWyXxrOwGogQd91nbGjLlv5aRNqLlBpWsr3yWMOi+RTXsSuaPqK9pWBWA1aaFuF1VnDbRV4fx4CkcGF3FlJmN+1Jg0gYun6FuptSky7YTmn6Y26ln3uQKBLfsQCAbxW3f34qZ11WYVHjsyg5cuxpFLTDvP/mUv61kLju8jwaMGq3pEiqVL1fMRQO3t6F6DpqgzEKDqEYvNCZ6ETwkOabPWqY5ME53GCIhciaxGpcBAcXmVSnt5fQLZzL5EkOsqAF/7bivU2zoPCkIlebOSbdUAVtNdeOcnUuisC5opfPliAufG0zazq0yxkiACOpfXrHFFZNJJsk4PetpieOt1jfiVm1sZq76SCZJf/9unxvHM6QWcHkrYU70lEKax5vt5aZrdRqiZeukRN1FE6tuwpjlkfEAxtcytsmUCTABKe0WwNAlLjCqu5yBKe6WtKsdRbpxdZyxbR5H22mAE8XOrN7maH/8/AAt8CZlORV+1TH59pduqAqx2fjSFM2MpM4MaQbo4mabWFjFME67yWvldzXKzZ00t3rhFdyDWmob8/7XjVxJWjP78+QTGp+MWCul5hHoGsJqeLSjN0dzUpkH+amzobTbTLFBlJiVsYsEGNM9LQ44CWUXr0jKFRwJRNVcSCI0whYMiiM5Ik2adVyfKxUhoTFGvAVdCU2ao6BL6AlnL+afjLPdTVv5xW3WAr21iyyJTGrbTHFYiLJq0Ux35v9R46jp53cl4gADLp8+RhGkWeaowO5K9TU0uExBtWCbIzY0N6G3yW4JCvl9MXn8iUUqmyDTr9hlNLaGXtFqjReolCYJ8sB7Lo1SrBESaLgDlXXUuImzSXme2W5E6B2i9uJlto5EmmX/F/Hqi20q2/5FyrHgTIVFWqLM+aEN0qq78XwZXjR2mjtvZG0Er2XlbnZd+1W+PeXUHY0ayBK4ALDNMqq2NoamapIvAKj1q2sV96JgCV6ArqaLBBlkYAUX5s8SLw/K1xAn1eFhqpOPD9ZJ5VtP5qDnhofNdf0JWIaD2Yhmtpd+JUa9ke1UB/pc3dVzZ6qQ0IXhdlTNBit+rigqtpjkNVrPzaVKjMSN4pjnsfOWWha4SKAYAm0K2yj1FYtNKkwapwQ5IFAKCIYavJq2VWRfIPAUD0PLP12hspcnOaBP7TEmoGE1to7TtShrRX3CAHa0QIDKnusnLRnwCJFNenwGmcWK3ZpcjC9a0wYp31ffExTRWnSvtVR/LPMvfCjhTMu5fgyIifsJVFkd/Ilv6vbbRD7VdBc9XgHW21XZaZlGwAemAWTHdHsbqSuisVPuFB9jpMJfVdClmlU8z02++0QOXV3Gs3wiTgJMuKZmiwQ5xAI3jqslVaJBBeWjVeDn3PRHggoCUiXZAFKjCyQY/DEwhqH8O0PqkdxUvmD/WNgrXtEYr7D+9OZqvbxLSimVY7vYLD7DTgWW0xpyHcQgk3S+k20sUzgRJczX8p2xZZbBCZlUgysxqGFEaqSpPG8Rnj2i5fLEYs0y5hEDbiElrP/K3YtdcZAAZuDwPe/G7oBZoDojy869osmm1qf5Ss33IVPPEhPgyt9cEwOpMTbFgc0yzJ6U56iuBUhtxxnSNCHF7W2+ftJ0zuK9Z68ScBaCExOrAaOoFikaNuNjYsliz9qXkhwZI9HthJcB0Hg5aApsfzdfaCdrR1Gwb/nvluzCt+GfdrrrcWgz8v6HionbyULQtAAAAAElFTkSuQmCC";
        } catch (IOException e){
            return null;
        }
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }
}
