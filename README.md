# JJJTP
FTPでファイルを取得する

## 使い方
接続先はsetting.xml に記載する。

<getlog>
	ルート要素

<savefolder>
	デフォルトのログファイル保存先

<filetypes>
	ログファイルの種類を複数定義可能

	<group>
		ログファイルグループ名を指定
		<filetype>
			以下の要素を指定する。

        <type>	ログファイル種類表示名
        <directory>	ログファイルの存在するディレクトリのフルパス。環境ごとに設定する文字列を、埋め込み書式で設定可能。(例:OpenTP1/{0}/log) 埋め込む文字列は、envs/env/machine/embed で指定する
        <keyword>	任意。デフォルトは”.*”。ログファイル検索条件を正規表現で指定する。<directory>以下を検索する。
        <depth>	任意。デフォルトは1。<directory>以下を再帰的に検索する階層の深さを指定する。

<envs>
    環境名を複数定義可能
    <env>
        環境名を属性で指定する
			<machine>
				サーバの情報を定義する。

            <name>	サーバ表示名
            <ip>	サーバのIPアドレス
            <user>	接続ユーザ
            <password>	接続パスワード
            <embed>	任意。埋め込み文字列を設定する
            <group>	ログファイルのグループを指定する。filetypes/group に一致するログファイル群を表示する。
